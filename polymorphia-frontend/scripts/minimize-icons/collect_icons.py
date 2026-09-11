#!/usr/bin/env python3
import re
import subprocess
import sys
from pathlib import Path

from fontTools.ttLib import TTFont

ROOT = Path(__file__).resolve().parent.parent.parent
SRC_FONT = ROOT / "node_modules/material-symbols/material-symbols-rounded.woff2"
SRC_DIRS = ["app", "components", "views", "hooks", "providers", "services",
            "interfaces", "animations"]

IDENT = r"[a-z][a-z0-9_]*"
JSX_INLINE = re.compile(rf">\s*({IDENT})\s*<")
JSX_BLOCK = re.compile(rf"^\s*({IDENT})\s*$", re.M)
QUOTED = re.compile(rf'"({IDENT})"')
ICON_ASSIGN = re.compile(rf'\bicon\s*[:=]\s*(?:"{IDENT}"|\{{[^}}]*\}})')
JSX_EXPR_CHILD = re.compile(r">\s*(\{[^{}]*\})\s*<")


def font_icon_names() -> set[str]:
    font = TTFont(SRC_FONT)
    rev = {g: chr(c) for c, g in font.getBestCmap().items()}
    names = set()
    for lookup in font["GSUB"].table.LookupList.Lookup:
        for sub in lookup.SubTable:
            inner = sub.ExtSubTable if lookup.LookupType == 7 else sub
            for first, lig_set in (getattr(inner, "ligatures", None) or {}).items():
                for lig in lig_set:
                    word = "".join(rev.get(g, "") for g in [first] + list(lig.Component))
                    if word:
                        names.add(word)
    return names


def candidates() -> set[str]:
    files = subprocess.check_output(
        ["find", *SRC_DIRS, "-name", "*.tsx", "-o", "-name", "*.ts"],
        text=True, cwd=ROOT,
    ).split()
    found: set[str] = set()
    for rel in files:
        text = (ROOT / rel).read_text(encoding="utf-8")
        found.update(JSX_INLINE.findall(text))
        found.update(JSX_BLOCK.findall(text))
        for m in ICON_ASSIGN.finditer(text):
            found.update(QUOTED.findall(m.group(0)))
        for m in JSX_EXPR_CHILD.finditer(text):
            found.update(QUOTED.findall(m.group(1)))
    return found


def main() -> int:
    if not SRC_FONT.exists():
        print(f"Missing {SRC_FONT} — run npm install", file=sys.stderr)
        return 1
    icons = sorted(candidates() & font_icon_names())
    print("\n".join(icons))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
