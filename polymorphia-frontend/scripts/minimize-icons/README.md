# Material Symbols Font Subsetting Tool

This directory contains utilities to subset the Google Material Symbols Rounded variable font from ~5.2 MB down to ~41 KB, retaining only the icons actually used in the frontend codebase.

## Prerequisites

Ensure you have Python 3 and the required dependencies installed:

```bash
pip install 'fonttools[woff]' brotli
```

## When to Use

- **Adding / Removing Icons**: Whenever you introduce a new Material Symbols icon in JSX/TSX or CSS, or remove an existing one.
- **CI / Quality Checks**: To ensure that the font subset always contains every icon used in the codebase before deploying or merging.

## Workflow

### 1. Update the Allowlist

Scan the codebase for all icon references and update `icons.allowlist`:

```bash
python3 scripts/minimize-icons/collect_icons.py > scripts/minimize-icons/icons.allowlist
```

### 2. Generate the SubsetFont

Build the minimized `.woff2` font file at `public/fonts/material-symbols-subset.woff2`:

```bash
./scripts/minimize-icons/build-icon-font.sh
```

### 3. Verify Icons Consistency

Run the verification guard (used in CI) to ensure the codebase and allowlist match:

```bash
./scripts/minimize-icons/check-icons.sh
```

## Files Description

- `collect_icons.py`: Scans all source directories for icon string candidates and intersects them with the font GSUB ligature table.
- `subset_icons.py`: Prunes ligature tables in the font and runs `fontTools.subset` to generate `public/fonts/material-symbols-subset.woff2` while preserving variable font axes (`FILL`, `wght`, `GRAD`, `opsz`).
- `build-icon-font.sh`: Shell script to execute `subset_icons.py`.
- `check-icons.sh`: Shell script comparing `icons.allowlist` with `collect_icons.py` output. Fails with non-zero exit code if discrepancies are found.
- `icons.allowlist`: Sorted list of icon names included in the generated subset.
