#!/home/brandon/miniforge3/bin/python3
"""
Bulk importer for WITS tariff data.

Usage example (after installing dependencies with `pip install psycopg[binary]`):

    python scripts/import_wits_bulk.py \
        --input-dir "2983760_49725EA1-0/AVEPref" \
        --db-host localhost \
        --db-port 5433 \
        --db-name tariffs \
        --db-user dev \
        --db-password devpass

The script streams every *.zip file, extracts the CSV inside without touching disk,
persists the selected columns into the `wits_tariffs` table, and writes lookup CSVs
for reporter / partner / product / nomenclature codes under `db/lookups/`.
"""

from __future__ import annotations

import argparse
import csv
import io
import sys
import time
from dataclasses import dataclass
from datetime import datetime
from decimal import Decimal
from pathlib import Path
from typing import Iterable, Iterator, Sequence
from zipfile import ZipFile

try:
    import psycopg  # psycopg 3.x
except ImportError as exc:  # pragma: no cover - import guard
    raise SystemExit(
        "psycopg is required. Install with `pip install psycopg[binary]`."
    ) from exc


CSV_COLUMNS = [
    "NomenCode",
    "Reporter_ISO_N",
    "Year",
    "ProductCode",
    "Partner",
    "Sum_Of_Rates",
    "Min_Rate",
    "Max_Rate",
    "SimpleAverage",
    "TotalNoOfLines",
    "Nbr_Pref_Lines",
    "Nbr_MFN_Lines",
    "Nbr_NA_Lines",
    "EstCode",
]

INSERT_SQL = """
INSERT INTO wits_tariffs (
    nomen_code,
    reporter_iso,
    partner_code,
    product_code,
    year,
    sum_of_rates,
    min_rate,
    max_rate,
    simple_average,
    total_no_of_lines,
    nbr_pref_lines,
    nbr_mfn_lines,
    nbr_na_lines,
    est_code,
    source_file
) VALUES (
    %(nomen_code)s,
    %(reporter_iso)s,
    %(partner_code)s,
    %(product_code)s,
    %(year)s,
    %(sum_of_rates)s,
    %(min_rate)s,
    %(max_rate)s,
    %(simple_average)s,
    %(total_no_of_lines)s,
    %(nbr_pref_lines)s,
    %(nbr_mfn_lines)s,
    %(nbr_na_lines)s,
    %(est_code)s,
    %(source_file)s
)
ON CONFLICT (nomen_code, reporter_iso, partner_code, product_code, year, est_code)
DO UPDATE SET
    sum_of_rates = EXCLUDED.sum_of_rates,
    min_rate = EXCLUDED.min_rate,
    max_rate = EXCLUDED.max_rate,
    simple_average = EXCLUDED.simple_average,
    total_no_of_lines = EXCLUDED.total_no_of_lines,
    nbr_pref_lines = EXCLUDED.nbr_pref_lines,
    nbr_mfn_lines = EXCLUDED.nbr_mfn_lines,
    nbr_na_lines = EXCLUDED.nbr_na_lines,
    source_file = EXCLUDED.source_file;
"""

AUDIT_TABLE_SQL = """
CREATE TABLE IF NOT EXISTS wits_import_audit (
    source_file TEXT PRIMARY KEY,
    finished_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
"""

UPSERT_COUNTRY_SQL = """
INSERT INTO wits_country_metadata (
    country_code,
    iso3,
    country_name,
    long_name,
    income_group,
    lending_category,
    region,
    currency_unit,
    is_group
) VALUES (
    %(country_code)s,
    %(iso3)s,
    %(country_name)s,
    %(long_name)s,
    %(income_group)s,
    %(lending_category)s,
    %(region)s,
    %(currency_unit)s,
    %(is_group)s
)
ON CONFLICT (country_code) DO UPDATE SET
    iso3 = EXCLUDED.iso3,
    country_name = EXCLUDED.country_name,
    long_name = EXCLUDED.long_name,
    income_group = EXCLUDED.income_group,
    lending_category = EXCLUDED.lending_category,
    region = EXCLUDED.region,
    currency_unit = EXCLUDED.currency_unit,
    is_group = EXCLUDED.is_group,
    updated_at = NOW();
"""

UPSERT_PRODUCT_SQL = """
INSERT INTO wits_product_metadata (
    nomen_code,
    product_code,
    tier,
    description
) VALUES (
    %(nomen_code)s,
    %(product_code)s,
    %(tier)s,
    %(description)s
)
ON CONFLICT (nomen_code, product_code) DO UPDATE SET
    tier = EXCLUDED.tier,
    description = EXCLUDED.description,
    updated_at = NOW();
"""


@dataclass(frozen=True)
class DbConfig:
    host: str
    port: int
    name: str
    user: str
    password: str


@dataclass(frozen=True)
class CountryMetadata:
    code: str
    iso3: str | None
    name: str | None
    long_name: str | None
    income_group: str | None
    lending_category: str | None
    region: str | None
    currency_unit: str | None
    is_group: bool


@dataclass(frozen=True)
class ProductMetadata:
    nomen_code: str
    product_code: str
    tier: int | None
    description: str


def chunked(items: Sequence, size: int) -> Iterator[Sequence]:
    for start in range(0, len(items), size):
        yield items[start : start + size]


def open_connection(db_cfg: DbConfig) -> "psycopg.Connection":
    conn = psycopg.connect(
        host=db_cfg.host,
        port=db_cfg.port,
        dbname=db_cfg.name,
        user=db_cfg.user,
        password=db_cfg.password,
    )
    conn.autocommit = False
    return conn


def ensure_import_audit(conn: "psycopg.Connection") -> set[str]:
    with conn.cursor() as cur:
        cur.execute(AUDIT_TABLE_SQL)
        try:
            cur.execute(
                """
                INSERT INTO wits_import_audit (source_file)
                SELECT DISTINCT source_file
                FROM wits_tariffs
                WHERE source_file IS NOT NULL
                ON CONFLICT DO NOTHING
                """
            )
        except psycopg.errors.UndefinedTable:
            # Fresh databases might not have the table yet; that's fine.
            pass
        cur.execute("SELECT source_file FROM wits_import_audit")
        return {row[0] for row in cur.fetchall() if row[0]}


def mark_file_imported(conn: "psycopg.Connection", source_file: str) -> None:
    with conn.cursor() as cur:
        cur.execute(
            """
            INSERT INTO wits_import_audit (source_file, finished_at)
            VALUES (%s, NOW())
            ON CONFLICT (source_file)
            DO UPDATE SET finished_at = EXCLUDED.finished_at
            """,
            (source_file,),
        )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Import WITS bulk tariff data.")
    parser.add_argument(
        "--input-dir",
        type=Path,
        help="Directory containing the WITS *.zip files (e.g. 2983760_.../AVEPref).",
    )
    parser.add_argument("--db-host", default="localhost")
    parser.add_argument("--db-port", type=int, default=5433)
    parser.add_argument("--db-name", default="tariffs")
    parser.add_argument("--db-user", default="dev")
    parser.add_argument("--db-password", default="devpass")
    parser.add_argument(
        "--batch-size",
        type=int,
        default=2000,
        help="Number of rows per batch insert.",
    )
    parser.add_argument(
        "--lookup-dir",
        type=Path,
        default=Path("lookups"),
        help="Where to write reporter/partner/product/nomen lookup CSVs.",
    )
    parser.add_argument(
        "--lookup-only",
        action="store_true",
        help="Skip reading ZIP files and only regenerate lookup CSVs from the existing database contents.",
    )
    parser.add_argument(
        "--max-file-retries",
        type=int,
        default=3,
        help="Maximum attempts per ZIP when the database connection drops.",
    )
    parser.add_argument(
        "--force",
        action="store_true",
        help="Re-import ZIP files even if they were previously processed successfully.",
    )
    parser.add_argument(
        "--country-metadata",
        type=Path,
        default=Path("lookups/wits_country_metadata.csv"),
        help="CSV exported from the WITS Country metadata workbook.",
    )
    parser.add_argument(
        "--hs-metadata",
        type=Path,
        default=Path("lookups/wits_hs_product_metadata.csv"),
        help="CSV exported from the WITS HS Products workbook.",
    )
    args = parser.parse_args()
    if not args.lookup_only and args.input_dir is None:
        parser.error("--input-dir is required unless --lookup-only is provided.")
    return args


def decimal_or_none(raw: str) -> Decimal | None:
    raw = raw.strip()
    return Decimal(raw) if raw else None


def int_or_none(raw: str) -> int | None:
    raw = raw.strip()
    return int(raw) if raw else None


def extract_rows(zip_path: Path) -> Iterator[dict]:
    """Yield cleaned rows from the CSV contained in `zip_path`."""
    with ZipFile(zip_path) as archive:
        csv_members = [name for name in archive.namelist() if name.lower().endswith(".csv")]
        if not csv_members:
            print(f"[WARN] No CSV found inside {zip_path.name}", file=sys.stderr)
            return
        csv_name = csv_members[0]
        with archive.open(csv_name) as raw_file:
            text_stream = io.TextIOWrapper(raw_file, encoding="utf-8-sig", newline="")
            reader = csv.DictReader(text_stream)
            missing = [col for col in CSV_COLUMNS if col not in reader.fieldnames]
            if missing:
                raise ValueError(f"{zip_path.name} missing expected columns: {missing}")
            for record in reader:
                yield {
                    "nomen_code": record["NomenCode"].strip(),
                    "reporter_iso": record["Reporter_ISO_N"].strip(),
                    "partner_code": record["Partner"].strip(),
                    "product_code": record["ProductCode"].strip(),
                    "year": int(record["Year"]),
                    "sum_of_rates": decimal_or_none(record["Sum_Of_Rates"]),
                    "min_rate": decimal_or_none(record["Min_Rate"]),
                    "max_rate": decimal_or_none(record["Max_Rate"]),
                    "simple_average": decimal_or_none(record["SimpleAverage"]),
                    "total_no_of_lines": int_or_none(record["TotalNoOfLines"]),
                    "nbr_pref_lines": int_or_none(record["Nbr_Pref_Lines"]),
                    "nbr_mfn_lines": int_or_none(record["Nbr_MFN_Lines"]),
                    "nbr_na_lines": int_or_none(record["Nbr_NA_Lines"]),
                    "est_code": record["EstCode"].strip() or None,
                }


def flush_batch(cur: "psycopg.Cursor", batch: list[dict]) -> int:
    if not batch:
        return 0
    cur.executemany(INSERT_SQL, batch)
    processed = len(batch)
    batch.clear()
    return processed


def import_zip_archive(
    conn: "psycopg.Connection",
    zip_path: Path,
    batch_size: int,
) -> int:
    inserted = 0
    with conn.cursor() as cur:
        batch: list[dict] = []
        for row in extract_rows(zip_path):
            row["source_file"] = zip_path.name
            batch.append(row)
            if len(batch) >= batch_size:
                inserted += flush_batch(cur, batch)
        inserted += flush_batch(cur, batch)
    return inserted


def import_file_with_retries(
    zip_path: Path,
    db_cfg: DbConfig,
    batch_size: int,
    max_attempts: int,
) -> int:
    attempt = 0
    while True:
        attempt += 1
        conn = open_connection(db_cfg)
        try:
            inserted = import_zip_archive(conn, zip_path, batch_size)
            mark_file_imported(conn, zip_path.name)
            conn.commit()
            return inserted
        except psycopg.OperationalError as exc:
            try:
                conn.rollback()
            except psycopg.Error:
                pass
            if attempt >= max_attempts:
                raise
            delay = min(2**attempt, 30)
            print(
                f"[WARN] {zip_path.name}: database connection dropped ({exc}). "
                f"Retrying in {delay}s ({attempt}/{max_attempts})"
            )
            time.sleep(delay)
        finally:
            conn.close()
def iter_zip_files(root: Path) -> Iterator[Path]:
    for path in sorted(root.glob("*.zip")):
        if path.is_file():
            yield path


def load_country_metadata(csv_path: Path) -> dict[str, CountryMetadata]:
    csv_path = csv_path.expanduser()
    if not csv_path.exists():
        print(f"[WARN] Country metadata file not found: {csv_path}", file=sys.stderr)
        return {}
    records: dict[str, CountryMetadata] = {}
    with csv_path.open(encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            code = (row.get("country_code") or "").strip()
            if not code:
                continue
            records[code] = CountryMetadata(
                code=code,
                iso3=(row.get("country_iso3") or "").strip() or None,
                name=(row.get("country_name") or "").strip() or None,
                long_name=(row.get("long_name") or "").strip() or None,
                income_group=(row.get("income_group") or "").strip() or None,
                lending_category=(row.get("lending_category") or "").strip() or None,
                region=(row.get("region") or "").strip() or None,
                currency_unit=(row.get("currency_unit") or "").strip() or None,
                is_group=not code.isdigit(),
            )
    return records


def load_product_metadata(csv_path: Path) -> dict[tuple[str, str], ProductMetadata]:
    csv_path = csv_path.expanduser()
    if not csv_path.exists():
        print(f"[WARN] HS product metadata file not found: {csv_path}", file=sys.stderr)
        return {}
    records: dict[tuple[str, str], ProductMetadata] = {}
    with csv_path.open(encoding="utf-8") as fh:
        reader = csv.DictReader(fh)
        for row in reader:
            nomen = (row.get("nomen_code") or "").strip()
            code = (row.get("product_code") or "").strip()
            if not nomen or not code:
                continue
            tier_raw = (row.get("tier") or "").strip()
            try:
                tier = int(tier_raw) if tier_raw else None
            except ValueError:
                tier = None
            description = (row.get("description") or "").strip()
            records[(nomen, code)] = ProductMetadata(
                nomen_code=nomen,
                product_code=code,
                tier=tier,
                description=description,
            )
    return records


def persist_country_metadata(
    conn: "psycopg.Connection", entries: Iterable[CountryMetadata], chunk_size: int = 1000
) -> int:
    rows = [
        {
            "country_code": entry.code,
            "iso3": entry.iso3,
            "country_name": entry.name,
            "long_name": entry.long_name,
            "income_group": entry.income_group,
            "lending_category": entry.lending_category,
            "region": entry.region,
            "currency_unit": entry.currency_unit,
            "is_group": entry.is_group,
        }
        for entry in entries
    ]
    if not rows:
        return 0
    with conn.cursor() as cur:
        for batch in chunked(rows, chunk_size):
            cur.executemany(UPSERT_COUNTRY_SQL, batch)
    return len(rows)


def persist_product_metadata(
    conn: "psycopg.Connection", entries: Iterable[ProductMetadata], chunk_size: int = 1000
) -> int:
    rows = [
        {
            "nomen_code": entry.nomen_code,
            "product_code": entry.product_code,
            "tier": entry.tier,
            "description": entry.description,
        }
        for entry in entries
    ]
    if not rows:
        return 0
    with conn.cursor() as cur:
        for batch in chunked(rows, chunk_size):
            cur.executemany(UPSERT_PRODUCT_SQL, batch)
    return len(rows)


def write_lookup_csv(output_dir: Path, name: str, values: Iterable[str]) -> None:
    output_dir.mkdir(parents=True, exist_ok=True)
    target = output_dir / f"{name}.csv"
    with target.open("w", newline="", encoding="utf-8") as fh:
        writer = csv.writer(fh)
        writer.writerow([name])
        for value in sorted(values):
            writer.writerow([value])


def export_lookups_from_db(conn: "psycopg.Connection", lookup_dir: Path) -> None:
    queries = {
        "reporter_codes": "SELECT DISTINCT reporter_iso FROM wits_tariffs",
        "partner_codes": "SELECT DISTINCT partner_code FROM wits_tariffs",
        "product_codes": "SELECT DISTINCT product_code FROM wits_tariffs",
        "nomen_codes": "SELECT DISTINCT nomen_code FROM wits_tariffs",
    }
    with conn.cursor() as cur:
        for name, sql in queries.items():
            cur.execute(sql)
            values = [row[0] for row in cur.fetchall() if row[0]]
            write_lookup_csv(lookup_dir, name, values)


def main() -> None:
    args = parse_args()
    if not args.lookup_only:
        input_dir: Path = args.input_dir.expanduser().resolve()
        if not input_dir.exists():
            raise SystemExit(f"Input directory not found: {input_dir}")
    else:
        input_dir = None

    db_cfg = DbConfig(
        host=args.db_host,
        port=args.db_port,
        name=args.db_name,
        user=args.db_user,
        password=args.db_password,
    )

    country_metadata = load_country_metadata(args.country_metadata)
    product_metadata = load_product_metadata(args.hs_metadata)

    if args.lookup_only:
        conn = open_connection(db_cfg)
        try:
            if country_metadata:
                count = persist_country_metadata(conn, country_metadata.values())
                if count:
                    print(f"[INFO] Upserted {count} country metadata rows")
            if product_metadata:
                count = persist_product_metadata(conn, product_metadata.values())
                if count:
                    print(f"[INFO] Upserted {count} HS metadata rows")
            conn.commit()
            export_lookups_from_db(conn, args.lookup_dir)
        finally:
            conn.close()
        print(f"[INFO] Lookup files written to {args.lookup_dir}")
        return

    state_conn = open_connection(db_cfg)
    try:
        processed_files = ensure_import_audit(state_conn)
        if country_metadata:
            count = persist_country_metadata(state_conn, country_metadata.values())
            if count:
                print(f"[INFO] Upserted {count} country metadata rows")
        if product_metadata:
            count = persist_product_metadata(state_conn, product_metadata.values())
            if count:
                print(f"[INFO] Upserted {count} HS metadata rows")
        state_conn.commit()
    finally:
        state_conn.close()

    total_rows = 0
    start_time = datetime.now()
    max_attempts = max(1, args.max_file_retries)
    assert input_dir is not None

    for zip_path in iter_zip_files(input_dir):
        if not args.force and zip_path.name in processed_files:
            print(f"[INFO] {zip_path.name}: already imported, skipping")
            continue
        inserted = import_file_with_retries(
            zip_path,
            db_cfg,
            args.batch_size,
            max_attempts,
        )
        processed_files.add(zip_path.name)
        total_rows += inserted
        print(f"[INFO] {zip_path.name}: inserted {inserted:,} rows")

    duration = (datetime.now() - start_time).total_seconds()
    print(f"[INFO] Completed import of {total_rows:,} new rows in {duration:.1f}s")

    lookup_conn = open_connection(db_cfg)
    try:
        export_lookups_from_db(lookup_conn, args.lookup_dir)
    finally:
        lookup_conn.close()
    print(f"[INFO] Lookup files written to {args.lookup_dir}")


if __name__ == "__main__":  # pragma: no cover
    main()
