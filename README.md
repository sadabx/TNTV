# T9TV Android TV

Native Android TV app for T9TV, rebuilt from the archived Flutter app state.

The current `main` branch is Kotlin-first and uses Jetpack Compose for the TV UI plus Media3/ExoPlayer for live HLS playback. The old Flutter project state is preserved on the `flutter-archive` branch.

## Tech Stack

- Kotlin
- Android TV / Leanback launcher support
- Jetpack Compose
- Media3 ExoPlayer
- Local channel logo assets from `assets/`

## Project Structure

- `android/` - native Android TV app module.
- `android/app/src/main/kotlin/com/tntv/tv/MainActivity.kt` - initial Compose TV shell, rail, channel cards, and player.
- `assets/` - channel logos and static media.

## Build

```bash
cd android
./gradlew :app:assembleDebug
```

## Notes

- The native app scaffold currently contains a small demo catalog so the TV navigation, white focus states, and playback surface can be built and tested first.
- The full channel catalog should be ported next from the website data source or archived Flutter data.
