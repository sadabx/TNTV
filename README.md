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
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── assets/
│       │   ├── iptv.png       # TRIONINE TV brand mark
│       │   └── logos/         # Active website channel-logo set
│       ├── kotlin/com/tntv/tv/
│       │   ├── MainActivity.kt
│       │   ├── Models.kt
│       │   ├── TvTheme.kt
│       │   └── ChannelCatalog.kt
│       ├── res/
│       └── AndroidManifest.xml
├── gradle/wrapper/
├── tools/                     # Catalogue and logo synchronization
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── README.md
└── ANDROID_TV_DESIGN.md
```

## Build

```bash
./gradlew :app:assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

Run all local verification before committing:

```bash
./gradlew :app:assembleDebug :app:lintDebug
```

## Run From VS Code

1. Open the repository root in VS Code.
2. Start an Android TV emulator or connect a TV with ADB debugging enabled.
3. Run **Terminal -> Run Task -> Android TV: Install Debug APK**.
4. Open TRIONINE TV from the device's Apps screen.

The checked-in tasks also provide separate **Build Debug APK** and **Lint** commands. They use Android Studio's bundled JDK at `/opt/android-studio/jbr`; adjust `.vscode/settings.json` and `.vscode/tasks.json` if your JDK is installed elsewhere.

## Navigation And Playback

- Starting a channel from Home, Search, or the guide opens its category panel beside playback.
- Selecting another guide channel recreates the player surface and switches both video and audio.
- Fullscreen hides the complete guide and expands video to the full viewport. Back exits fullscreen before leaving playback.
- Vertical D-pad movement chooses the nearest card in the adjacent row and scrolls off-screen destinations into focus.
- Stream failure advances through available sources before showing a compact unavailable state.

## Website Source

- Website repository: [`sadabx/iptv`](https://github.com/sadabx/iptv)
- Authoritative catalogue file: `js/channel-catalog.js`
- Stable app feed: `https://sadabx.github.io/TNTV/data/channels.json`
- Stable update feed: `https://sadabx.github.io/TNTV/data/app-update.json`
- Android generated snapshot: `app/src/main/kotlin/com/tntv/tv/ChannelCatalog.kt`
- Packaged logo assets: `app/src/main/assets/logos/`

## Catalogue Sync

The website file `js/channel-catalog.js` from [`sadabx/iptv`](https://github.com/sadabx/iptv) is authoritative. Android also loads `https://sadabx.github.io/TNTV/data/channels.json` at startup so stream fixes can ship from the TNTV GitHub Pages feed without a new APK. The generated Kotlin snapshot remains the instant/offline fallback. Android must preserve category order, channel order, IDs, names, short names, logo paths, source labels, and stream URLs. The current Android snapshot contains 94 channels across 9 categories.

When the website catalogue changes:

1. Regenerate `ChannelCatalog.kt` from `CHANNELS_DATA`; do not maintain a second hand-edited list.
2. Publish the mirrored JSON to `data/channels.json` on the TNTV GitHub Pages site.
3. Replace `app/src/main/assets/logos/` with the website's active logo set.
4. Confirm every local `logo` path resolves and no unused logo remains.
5. Build the debug APK and test focus traversal at 720p, 1080p, and 4K.

From this repository root, the checked-in sync utility performs steps 1-3:

```bash
./tools/sync-from-website.sh /absolute/path/to/sadabx/iptv
```

## App Updates

The app checks `https://sadabx.github.io/TNTV/data/app-update.json` at startup. If the remote `versionCode` is greater than the installed APK's `BuildConfig.VERSION_CODE`, TNTV shows a compact update prompt, downloads the APK, verifies `sha256` when present, and opens Android's package installer. Installation still requires user approval because this is a normal sideloaded app.

## Product Rule

The reliable channel catalogue is the foundation. Popular matches are optional enrichment: load them asynchronously, insert them only after confirmed results, and silently omit the section on empty data, timeout, or failure.

See [ANDROID_TV_DESIGN.md](ANDROID_TV_DESIGN.md) before changing layout, focus behavior, navigation, or player controls.
