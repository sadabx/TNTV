# TRIONINE TV for Android TV

Native Android TV client for the TRIONINE live-channel catalogue. The app mirrors the website's visual language and interaction model while using TV-native focus navigation and Media3 playback.

The website version lives in [`sadabx/iptv`](https://github.com/sadabx/iptv). That repository is the source of truth for the channel catalogue, ordering, stream URLs, and logo assets used by this Android TV app.

## Current Stack

- Kotlin and Jetpack Compose
- Android TV / Leanback launcher support
- Media3 ExoPlayer with HLS support
- Coil with SVG support for local and remote logos
- Website catalogue and logos as the content source of truth

## Project Structure

```text
.
├── README.md
├── ANDROID_TV_DESIGN.md       # Detailed visual, focus, playback, and QA contract
├── assets/
│   ├── iptv.png               # TRIONINE TV brand mark
│   └── logos/                 # Exact active website channel-logo set
└── android/
    ├── app/src/main/kotlin/com/tntv/tv/
    │   ├── MainActivity.kt    # Compose shell, guide, browse rows, search, player
    │   └── ChannelCatalog.kt  # Generated website catalogue snapshot
    └── app/src/main/res/      # Launcher, TV banner, and Android resources
```

## Build

```bash
cd android
./gradlew :app:assembleDebug
```

The debug APK is generated at `android/app/build/outputs/apk/debug/app-debug.apk`.

## Website Source

- Website repository: [`sadabx/iptv`](https://github.com/sadabx/iptv)
- Authoritative catalogue file: `js/channel-catalog.js`
- Android generated snapshot: `android/app/src/main/kotlin/com/tntv/tv/ChannelCatalog.kt`
- Shared logo assets: `assets/logos/`

## Catalogue Sync

The website file `js/channel-catalog.js` from [`sadabx/iptv`](https://github.com/sadabx/iptv) is authoritative. Android must preserve category order, channel order, IDs, names, short names, logo paths, source labels, and stream URLs. The current Android snapshot contains 94 channels across 9 categories.

When the website catalogue changes:

1. Regenerate `ChannelCatalog.kt` from `CHANNELS_DATA`; do not maintain a second hand-edited list.
2. Replace `assets/logos/` with the website's active logo set.
3. Confirm every local `logo` path resolves and no unused logo remains.
4. Build the debug APK and test focus traversal at 720p, 1080p, and 4K.

From this repository root, the checked-in sync utility performs steps 1-3:

```bash
./tools/sync-from-website.sh /absolute/path/to/sadabx/iptv
```

## Product Rule

The reliable channel catalogue is the foundation. Popular matches are optional enrichment: load them asynchronously, insert them only after confirmed results, and silently omit the section on empty data, timeout, or failure.

See [ANDROID_TV_DESIGN.md](ANDROID_TV_DESIGN.md) before changing layout, focus behavior, navigation, or player controls.
