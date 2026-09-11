#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/../.."

if ! python3 -c 'import fontTools, brotli' 2>/dev/null; then
  echo "Dependency missing. Install: pip install 'fonttools[woff]' brotli" >&2
  exit 1
fi

if ! diff -u scripts/minimize-icons/icons.allowlist <(python3 scripts/minimize-icons/collect_icons.py); then
  echo
  echo "Material symbols list diverged with scripts/minimize-icons/icons.allowlist."
  echo "Please update allowlist and rerun script:"
  echo "  python3 scripts/minimize-icons/collect_icons.py > scripts/minimize-icons/icons.allowlist"
  echo "  ./scripts/minimize-icons/build-icon-font.sh"
  exit 1
fi
echo "Icons aligned with allowlist on ($(wc -l < scripts/minimize-icons/icons.allowlist | tr -d ' ') ) positions."
