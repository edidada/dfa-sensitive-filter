#!/usr/bin/env python3
"""Normalize, de-duplicate and sort a sensitive-word list.

Usage:
    python scripts/gen-wordlist.py raw1.txt raw2.txt -o words.txt

Normalization (mirrors CharNormalizer.java, but this offline script may use NFKC
because it rewrites the dictionary *before* loading, where index stability against
the input text is irrelevant):

  * Unicode NFKC  -> full-width ASCII becomes half-width, ideographic space -> space
  * lower-case
  * strip surrounding whitespace

Blank lines and lines starting with '#' are dropped. Output is UTF-8, sorted, unique.
"""
import argparse
import sys
import unicodedata


def normalize(line: str) -> str:
    return unicodedata.normalize("NFKC", line).strip().lower()


def main() -> int:
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument("inputs", nargs="+", help="raw word list files (UTF-8)")
    ap.add_argument("-o", "--output", help="output file (default: stdout)")
    args = ap.parse_args()

    words = set()
    for path in args.inputs:
        with open(path, "r", encoding="utf-8") as fh:
            for line in fh:
                w = normalize(line)
                if not w or w.startswith("#"):
                    continue
                words.add(w)

    out = sys.stdout if not args.output else open(args.output, "w", encoding="utf-8")
    try:
        for w in sorted(words):
            print(w, file=out)
    finally:
        if args.output:
            out.close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
