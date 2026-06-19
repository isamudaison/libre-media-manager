#!/usr/bin/env python3
"""Export a live OpenAPI document from a running Libre Media Manager instance."""

from __future__ import annotations

import argparse
import json
import sys
import urllib.error
import urllib.request
from pathlib import Path


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--url",
        default="http://127.0.0.1:8080/v3/api-docs",
        help="OpenAPI endpoint to fetch. Default: %(default)s",
    )
    parser.add_argument(
        "--output",
        default="clients/spec/openapi-v1.json",
        help="Where to write the fetched JSON. Default: %(default)s",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()
    try:
        with urllib.request.urlopen(args.url, timeout=5) as response:
            body = response.read()
    except urllib.error.URLError as exc:
        print(f"Failed to fetch OpenAPI from {args.url}: {exc}", file=sys.stderr)
        return 1

    parsed = json.loads(body)
    output_path = Path(args.output)
    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(parsed, indent=2) + "\n", encoding="utf-8")
    print(f"Wrote {output_path}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
