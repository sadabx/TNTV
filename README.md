# TNTV 

TNTV is a Flutter-based IPTV and streaming app with a polished dark UI, channel browsing, live playback, short-form video, favorites, and profile settings.

## Features

- Live TV channel browsing with search and category filtering.
- Full-screen video playback with fallback stream handling.
- Shorts-style vertical video feed.
- Favorites and watch history persisted with `shared_preferences`.
- Profile customization, including username and theme selection.
- Multiple app themes and a glassmorphic bottom navigation layout.

## Tech Stack

- Flutter
- `video_player`
- `shared_preferences`
- `flutter_svg`

## Project Structure

- `lib/main.dart` - app bootstrap and state wiring.
- `lib/screens/` - home, player, shorts, settings, splash, and layout screens.
- `lib/data/` - channel catalog and stream metadata.
- `lib/models/` - channel, stream, and category models.
- `lib/providers/` - app state and persistence logic.
- `lib/theme/` - app theme definitions.
- `assets/` - logos and static media used by the UI.
- `test/` - widget tests.

```text
TNTV/
├── android/
├── assets/
│   └── logos/
├── ios/
├── lib/
│   ├── data/
│   ├── models/
│   ├── providers/
│   ├── screens/
│   ├── theme/
│   └── main.dart
├── test/
├── web/
├── windows/
└── linux/
```

## Getting Started

### Prerequisites

- Flutter SDK installed and available on your path.
- Android Studio, VS Code, or another Flutter-compatible editor.

### Run the app

```bash
flutter pub get
flutter run
```

### Build release APK

```bash
flutter build apk --release
```

## Notes

- Stream availability depends on the configured source URLs in `lib/data/channels_data.dart`.
- User preferences such as theme, username, favorites, and playback settings are stored locally on the device.
