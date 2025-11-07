#!/usr/bin/env python3
"""
Scan the downloaded WITS ZIP archives and report the most common HS6 product
codes. Helps decide which products to seed into the `products` table.
"""

from __future__ import annotations

import argparse
import csv
import io
from collections import Counter
from pathlib import Path
from typing import Iterable
from zipfile import ZipFile


def iter_zip_files(root: Path) -> Iterable[Path]:
    for path in sorted(root.glob("*.zip")):
        if path.is_file():
            yield path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Report most common HS product codes.")
    parser.add_argument(
        "--zip-dir",
        type=Path,
        default=Path("2983760_49725EA1-0/AVEPref"),
        help="Directory containing WITS *.zip files.",
    )
    parser.add_argument(
        "--limit",
        type=int,
        default=50,
        help="Number of top HS codes to print.",
    )
    parser.add_argument(
        "--reporter",
        action="append",
        help="Filter by Reporter_ISO_N (e.g. 702 for Singapore). May be repeated.",
    )
    parser.add_argument(
        "--partner",
        action="append",
        help="Filter by Partner code. May be repeated.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    root = args.zip_dir.expanduser().resolve()
    if not root.exists():
        raise SystemExit(f"ZIP directory not found: {root}")

    reporters = set(args.reporter or [])
    partners = set(args.partner or [])

    counts: Counter[str] = Counter()

    for zip_path in iter_zip_files(root):
        with ZipFile(zip_path) as archive:
            csv_members = [name for name in archive.namelist() if name.lower().endswith(".csv")]
            if not csv_members:
                continue
            csv_name = csv_members[0]
            with archive.open(csv_name) as raw_file:
                reader = csv.DictReader(io.TextIOWrapper(raw_file, encoding="utf-8-sig"))
                for row in reader:
                    reporter_iso = row["Reporter_ISO_N"].strip()
                    partner_code = row["Partner"].strip()
                    if reporters and reporter_iso not in reporters:
                        continue
                    if partners and partner_code not in partners:
                        continue
                    hs_code = row["ProductCode"].strip()
                    if hs_code:
                        counts[hs_code] += 1

    if not counts:
        print("No matches found for the given filters.")
        return

    print("hs_code,count")
    for hs_code, count in counts.most_common(args.limit):
        print(f"{hs_code},{count}")


if __name__ == "__main__":
    main()
