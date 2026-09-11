#!/usr/bin/env python3
import sys
from pathlib import Path

from fontTools.ttLib import TTFont
from fontTools.subset import Subsetter, Options

ROOT = Path(__file__).resolve().parent.parent.parent
SRC = ROOT / "node_modules/material-symbols/material-symbols-rounded.woff2"
OUT = ROOT / "public/fonts/material-symbols-subset.woff2"
ALLOWLIST = ROOT / "scripts/minimize-icons/icons.allowlist"

REQUIRED_AXES = {"FILL", "wght", "GRAD", "opsz"}


def prune_ligatures(font: TTFont, keep: set[str]) -> int:
    kept = 0
    lookups = font["GSUB"].table.LookupList.Lookup
    for lookup in lookups:
        for sub in lookup.SubTable:
            inner = sub.ExtSubTable if lookup.LookupType == 7 else sub
            ligatures = getattr(inner, "ligatures", None)
            if not ligatures:
                continue
            for first, lig_set in list(ligatures.items()):
                survivors = [lg for lg in lig_set if lg.LigGlyph in keep]
                if survivors:
                    ligatures[first] = survivors
                    kept += len(survivors)
                else:
                    del ligatures[first]
    return kept


def main() -> int:
    icons = [line.strip() for line in ALLOWLIST.read_text().splitlines() if line.strip()]
    if not icons:
        print(f"{ALLOWLIST} is empty", file=sys.stderr)
        return 1

    font = TTFont(SRC)

    glyphs = set(font.getGlyphOrder())
    missing = [i for i in icons if i not in glyphs]
    if missing:
        print(f"Missing icons: {', '.join(missing)}", file=sys.stderr)
        return 1

    kept = prune_ligatures(font, set(icons))
    print(f"Ligatures saved in GSUB: {kept} (from {len(icons)} icons)")

    options = Options()
    options.flavor = "woff2"
    options.hinting = False
    options.desubroutinize = False
    options.layout_features = ["rlig", "rclt", "liga", "dlig", "calt", "ccmp", "rvrn"]
    options.name_IDs = ["*"]
    options.notdef_outline = True
    options.recalc_bounds = True
    options.retain_gids = False

    sub = Subsetter(options=options)
    sub.populate(glyphs=icons, text="".join(icons) + " _0123456789")
    sub.subset(font)

    OUT.parent.mkdir(parents=True, exist_ok=True)
    font.save(OUT)

    out_font = TTFont(OUT)
    axes = {a.axisTag for a in out_font["fvar"].axes} if "fvar" in out_font else set()
    if not REQUIRED_AXES <= axes:
        print(f"Lost: {REQUIRED_AXES - axes}", file=sys.stderr)
        return 1

    src_kb = SRC.stat().st_size / 1024
    out_kb = OUT.stat().st_size / 1024
    print(f"Variable axes: {sorted(axes)}")
    print(f"{SRC.name}: {src_kb:,.0f} KB  ->  {OUT.name}: {out_kb:,.1f} KB "
          f"(-{100 - 100 * out_kb / src_kb:.1f}%)")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
