# TRIONINE Android TV Design Contract

This document translates the current TRIONINE website into a native Android TV experience. Match the website's hierarchy, density, colors, cards, and player controls, but use Android TV focus semantics rather than browser hover behavior.

The website version lives in [`sadabx/iptv`](https://github.com/sadabx/iptv). Its `js/channel-catalog.js` file and active logo assets are the source of truth for the Android catalogue snapshot.

## 1. Experience Principles

- Channels render immediately and never wait for the popular-match service.
- Focus must always be visible, predictable, and reachable with a five-way remote.
- Right-arrow moves right. It must never activate or expand the currently focused rail item.
- Center/Enter performs the focused action. Back closes the deepest open layer first.
- The player gets the largest possible surface. Navigation and controls appear only when needed.
- Empty or failed optional services disappear quietly. Do not show homepage failure copy.
- Do not reproduce mobile bottom navigation on TV; the floating guide rail is the TV navigation model.

## 2. Visual Tokens

Use these values as the Android equivalents of the website theme:

| Token | Value | Use |
| --- | --- | --- |
| Background | `#06070B` | App and browse surface |
| Rail | `#0C1016` at 95% | Floating navigation rail |
| Panel | `#090B10` at 98% | Expanded guide and menus |
| Elevated | `#181C26` | Search, menus, selected rows |
| Primary text | `#F7F8FB` | Titles and active labels |
| Secondary text | 72% primary | Channel and navigation labels |
| Muted text | 48% primary | Counts and metadata |
| Accent | `#35D6A4` | Focus border and active state |
| Live | `#FF0000` | Player status and optional match cards |
| Border | 12% white | Rail, cards, panels, controls |

Use system sans/Roboto on Android TV. Titles are heavy, labels are semibold, and letter spacing remains zero. Avoid gradients as decoration; use the dark surfaces and content logos to carry the UI.

## 3. Responsive TV Metrics

Support 720p, 1080p, and 4K without hard-coding one screen size. The Compose implementation maps the website's measured CSS geometry to Android TV dp using a 960 dp wide baseline, which corresponds to a common 1920x1080 TV density report. Clamp the scale between 0.78 and 2.0 so compact and 4K surfaces remain usable.

Baseline metrics at 960 dp width:

- Rail slot: 37 dp; collapsed rail: 31 dp.
- Expanded category menu: 160 dp.
- Open channel guide: 37 dp rail plus 141 dp channel panel.
- Rail icon tile: 23 dp; rail icon glyph: 11 dp.
- Screen content inset: 17 dp.
- Search width: 220 dp.
- Channel card width: 125 dp with a stable 16:9 image region.
- Horizontal card gap: 8 dp.
- Category row gap: 17 dp.

Cards and controls must not change their layout footprint when focused. Scale may reach 1.05-1.08 only when surrounding spacing prevents clipping.

## 4. Navigation Rail

### Collapsed

- Float 8 dp from the top, left, and bottom edges.
- Use a rounded dark shell with a subtle white border.
- Order: menu, Home, Search, category icons.
- Show the TRIONINE TV vertical wordmark near the bottom when space permits.
- Home is always present.
- The selected category uses a faint emerald background, emerald icon, and low-opacity border. It must not look keyboard-focused after the guide closes.
- Keyboard focus uses a white tile, dark icon, and emerald outline.

### Expanded categories

- Replace the menu icon with Close.
- Keep the icon rail attached to a dark guide panel inside one floating shell.
- Show TRIONINE branding at the top. `TV` is smaller and blue.
- Rows show icon, category name, channel count, and a right chevron.
- Center/Enter opens a category. Right may move into the panel but cannot trigger the rail item itself.

### Expanded channel list

- Header contains Back, category name, count, and optional visibility/status action.
- Each row shows a white logo tile and channel name.
- Focus uses an elevated dark/white treatment with an emerald leading edge or outline.
- Selecting a channel starts playback and keeps its category available in the guide.

## 5. Home Catalogue

- Start directly with the category catalogue; do not add a marketing hero or redundant top bar.
- Each category is an unframed section with a title and horizontal `LazyRow`.
- Channel cards use a white 16:9 logo tile and one-line channel name below. Do not add repetitive LIVE badges.
- Preserve website category order and channel order.
- Left/right moves within a row. Up/down moves to the closest column in the adjacent row.
- Up from the first row reaches Search. Back from Home exits according to Android TV conventions.
- Home rows are never filtered by a previous search. Clear the query when playback begins or Home is selected.

## 6. Search

- Search opens as an independent guide panel, not a permanent rail text field.
- Focus the text input when the search panel opens so the Android TV keyboard can appear.
- Filter by channel name and category, case-insensitively.
- Results update as text changes and use the same channel-row component as category lists.
- Selecting a result clears the query, starts playback, and opens the channel's real category guide.
- A local no-results line is acceptable inside Search. Never filter or replace Home sections.

## 7. Popular Matches

Popular matches are a bonus section above Sports, never the app foundation.

1. Render channel rows immediately.
2. Request the external match source in the background with a 2-4 second timeout.
3. While loading, either render nothing or reserve only a short skeleton; never show a large loading headline.
4. When live matches exist, insert a compact `POPULAR MATCHES` horizontal carousel above Sports.
5. Match cards show LIVE, title, competition/category, poster or team badges, and `Watch now`.
6. If no matches exist or the source fails, log internally and render no heading, container, placeholder, message, or empty space.
7. Never block the catalogue or player on the external source.

## 8. Player

- Playback replaces the browse surface and opens the active channel's category guide by default.
- Selecting another guide channel must replace both the player video surface and audio source immediately.
- Do not retain the Home top bar while watching.
- Media uses fit behavior by default; never crop live video to fill the screen.
- Bottom controls use a translucent black strip: Play/Pause, LIVE, system volume affordance, channel title, Source, Quality, and Fullscreen/status at the far right.
- Do not add a custom PiP button; the platform/browser behavior is sufficient and PiP is not core to TV use.
- Source opens a compact focusable menu and highlights the active source. Switching source rebuilds playback without leaving the channel.
- Quality defaults to Auto. Its menu should list only tracks actually reported by Media3 and allow returning to Auto.
- Fullscreen hides the rail and channel panel, expands video to the full viewport, and changes the control to an exit-fullscreen icon. Back exits fullscreen first.
- Hide controls after inactivity and restore them on remote input.
- On stream failure, try the next source once, then show a compact retry/source choice while keeping Back and guide navigation functional.
- YouTube and third-party embed URLs require a dedicated provider path; do not pass a webpage URL directly to ExoPlayer.

## 9. Branding

- Product name is `TRIONINE TV`, never `T9TV` or `TNTV` in user-facing UI.
- `TRIONINE` is primary white text; `TV` is smaller and blue.
- Use `app/src/main/assets/iptv.png` until a dedicated Android adaptive icon and 320x180 TV banner are supplied.
- Do not show vertical rail branding when a full top/expanded-guide brand is visible.

## 10. Data and Logos

- `js/channel-catalog.js` in [`sadabx/iptv`](https://github.com/sadabx/iptv) is the edit-time catalogue source of truth.
- `https://sadabx.github.io/TNTV/data/channels.json` is the stable runtime update feed for Android; keep the generated Kotlin snapshot as the instant/offline fallback.
- `https://sadabx.github.io/TNTV/data/app-update.json` is the stable APK update feed; updates are optional prompts and must never block catalog browsing or playback.
- Catalogue logo values remain `assets/logos/<file>`. Files are packaged from `app/src/main/assets/logos/` and loaded through `file:///android_asset/logos/<file>`.
- Coil SVG support is required because the catalogue contains SVG logos.
- Keep only logos referenced by active channels. Remote logos may be used only when no maintained local asset exists.
- Trim accidental whitespace from stream URLs during generation.

## 11. Accessibility and Remote QA

Every interactive element needs a content description or meaningful text. Focus must have at least two signals, such as color plus border. Text and controls must retain adequate contrast.

Verify these paths with a keyboard and a real/emulated TV remote:

1. Cold launch -> first card -> right across row -> down to next row -> up to Search.
2. Rail menu -> categories -> category -> channel -> playback.
3. Collapsed selected category -> Right moves toward content and does not reopen the category.
4. Search -> type -> result -> playback -> Home; Home shows the full unfiltered catalogue.
5. Player with category guide -> channel switch -> source -> quality -> fullscreen -> Back.
6. Broken first source -> fallback/retry remains usable.
7. Optional match API empty, slow, and failed -> Sports remains first and fully usable.
8. No card, focus ring, menu, or player control clips at 1280x720, 1920x1080, or 3840x2160.

## 12. Definition of Done

- Android catalogue count, IDs, ordering, source labels, and URLs match the website snapshot.
- Every active local logo exists and every logo file is referenced.
- No user-facing `T9TV`/`TNTV` branding remains.
- D-pad movement never performs an action except where the control explicitly defines directional navigation.
- Search is functional and cannot persistently filter Home.
- Player supports reliable channel switching, source selection, Auto quality reset, fit rendering, working fullscreen, and no custom PiP.
- Popular matches fail silently and cannot delay channels.
- `./gradlew :app:assembleDebug` succeeds.
