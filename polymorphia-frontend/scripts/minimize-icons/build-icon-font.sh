#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."

if ! python3 -c 'import fontTools, brotli' 2>/dev/null; then
  echo "Dependency missing. Install: pip install 'fonttools[woff]' brotli" >&2
  exit 1
fi

python3 scripts/minimize-icons/subset_icons.py
