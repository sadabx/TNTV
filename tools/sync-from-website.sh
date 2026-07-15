#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 /absolute/path/to/iptv-website" >&2
  exit 64
fi

project_root="$(cd "$(dirname "$0")/.." && pwd)"
website_root="$(cd "$1" && pwd)"

node "$project_root/tools/generate-channel-catalog.js" \
  "$website_root/js/channel-catalog.js" \
  "$project_root/android/app/src/main/kotlin/com/tntv/tv/ChannelCatalog.kt"

rsync -a --delete "$website_root/assets/logos/" "$project_root/assets/logos/"
cp "$website_root/assets/iptv.png" "$project_root/assets/iptv.png"

echo "Synced Android catalogue and logos from $website_root"
