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
import os
import sys
from collections import defaultdict
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


@dataclass(frozen=True)
class DbConfig:
    host: str
    port: int
    name: str
    user: str
    password: str


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


def iter_zip_files(root: Path) -> Iterator[Path]:
    for path in sorted(root.glob("*.zip")):
        if path.is_file():
            yield path


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

    conn = psycopg.connect(
        host=db_cfg.host,
        port=db_cfg.port,
        dbname=db_cfg.name,
        user=db_cfg.user,
        password=db_cfg.password,
    )
    conn.autocommit = False

    if args.lookup_only:
        export_lookups_from_db(conn, args.lookup_dir)
        print(f"[INFO] Lookup files written to {args.lookup_dir}")
        return

    reporters: set[str] = set()
    partners: set[str] = set()
    products: set[str] = set()
    nomen_codes: set[str] = set()

    total_rows = 0
    start_time = datetime.now()

    def flush_batch(cur, batch: list[dict]) -> int:
        if not batch:
            return 0
        cur.executemany(INSERT_SQL, batch)
        count = len(batch)
        batch.clear()
        return count

    with conn:
        with conn.cursor() as cur:
            batch: list[dict] = []
            assert input_dir is not None
            for zip_path in iter_zip_files(input_dir):
                processed = 0
                for row in extract_rows(zip_path):
                    row["source_file"] = zip_path.name
                    reporters.add(row["reporter_iso"])
                    partners.add(row["partner_code"])
                    products.add(row["product_code"])
                    nomen_codes.add(row["nomen_code"])
                    batch.append(row)
                    if len(batch) >= args.batch_size:
                        inserted = flush_batch(cur, batch)
                        total_rows += inserted
                        processed += inserted
                inserted = flush_batch(cur, batch)
                total_rows += inserted
                processed += inserted
                print(f"[INFO] {zip_path.name}: inserted {processed:,} rows")
        conn.commit()

    duration = (datetime.now() - start_time).total_seconds()
    print(f"[INFO] Completed import of {total_rows:,} rows in {duration:.1f}s")

    # Write lookup CSVs for frontend/backend mappings.
    lookup_dir: Path = args.lookup_dir
    write_lookup_csv(lookup_dir, "reporter_codes", reporters)
    write_lookup_csv(lookup_dir, "partner_codes", partners)
    write_lookup_csv(lookup_dir, "product_codes", products)
    write_lookup_csv(lookup_dir, "nomen_codes", nomen_codes)
    print(f"[INFO] Lookup files written to {lookup_dir}")


if __name__ == "__main__":  # pragma: no cover
    main()
