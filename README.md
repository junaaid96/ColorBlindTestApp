# Color Vision Lab

Private, offline colour vision screening and tools for **Windows, macOS, Linux, Android, iOS and the web**.
Built with [Tauri v2](https://v2.tauri.app), React and Rust.

[Website](https://color-vision-lab-codejborg.vercel.app) · [Releases](https://github.com/junaaid96/color-vision-lab/releases) · [Privacy](PRIVACY.md) · [Design notes](docs/DESIGN.md)

> Screening, not a diagnosis. Screens vary in colour and brightness. If you are concerned about your vision, see an eye-care professional.

<p align="center">
  <picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/home-dark.webp"><img src="docs/screenshots/desktop/home-light.webp" alt="Home screen on desktop" width="62%"></picture>
  <picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/home-dark.webp"><img src="docs/screenshots/phone/home-light.webp" alt="Home screen on a phone" width="20%"></picture>
</p>
<p align="center"><a href="#screenshots--features-in-action">All screenshots: phone, tablet and desktop, light and dark ↓</a></p>

## Features

| | |
|---|---|
| **Ishihara-style plates** | Quick 7-plate or full 16-plate screening. Plates are generated fresh from Machado confusion lines with lightness noise, so they can't be memorised. Control, red-green, tritan and protan/deutan diagnostic plates. |
| **Results report** | Type, severity, confidence and a per-plate breakdown. Share it, or export a printable HTML report (print to PDF). |
| **D-15 hue arrangement** | Vingrys & King-Smith scoring (C-index, S-index, axis angle, crossings). Tap, drag or use the keyboard. |
| **Contrast sensitivity** | Four-direction arrow with an adaptive 1-up/2-down staircase; reports log CS. |
| **Color Lens** | Live camera (or a photo) with the colour name under the crosshair, protan/deutan/tritan simulation or enhancement, freeze frame, spoken names and haptics. |
| **Photo simulator** | Original and all three types side by side, adjustable severity, daltonize mode, save any panel as PNG. Pixels are processed in Rust in the app and in TypeScript on the web. |
| **History** | Trend charts per test, CSV export, JSON backup and restore, clear. |
| **Daily practice** | 60-second odd-hue game with levels, lives, best score and a daily streak. |
| **Accessibility** | Okabe-Ito palette, high contrast, reduced motion, screen-reader labels and live regions, full keyboard support (`1`–`7` switch sections, `?` shows shortcuts, digits/Enter answer plates, arrows answer contrast). |
| **Languages** | English, বাংলা, Español, हिन्दी (English fallback for untranslated strings). |
| **Themes** | System, light and dark. |
| **Calibration guidance** | A grey-step check and screen tips before plate tests. |
| **Offline** | Native apps bundle everything. The web build caches itself with a service worker. |
| **Desktop extras** | Signed auto-updates from GitHub Releases, remembered window size and position, single instance, `colorvisionlab://<section>` deep links, native notifications. |

### New in 3.0 compared with the Kotlin 2.0 app
Desktop, iOS and web versions; keyboard shortcuts; HTML report export; CSV export and JSON import of history; photo mode in Color Lens; desktop auto-update; deep links; a Rust pixel pipeline. The Kotlin/Compose Android app is preserved at the [`kotlin-v2.0.0`](https://github.com/junaaid96/color-vision-lab/tree/kotlin-v2.0.0) tag.

## Screenshots — features in action

Every screen of the app, captured from the web build at phone, tablet and desktop sizes. The images follow your GitHub theme (light or dark). The Windows, macOS, Linux, Android and iOS apps render this same interface in the system WebView; these are not photos of real devices. All files are in [`docs/screenshots/`](docs/screenshots/) (`<device>/<feature>-<light|dark>.webp`).

| Feature | What it shows |
|---|---|
| Home | Home with tests, streak and last score |
| Onboarding | First-run onboarding: screen, don't diagnose |
| Tests | All tests plus the before-you-start checklist |
| Quick screening | Quick screening plate with the on-screen keypad |
| Hue arrangement | D-15 style hue arrangement with draggable caps |
| Contrast sensitivity | Adaptive arrow staircase contrast test |
| Colour lens | Live colour lens naming the colour under the crosshair |
| Photo simulator | Photo shown as protan, deutan and tritan |
| Daily practice | 60-second odd-hue practice game |
| History | History with CSV and JSON backup export |
| Result report | Result report with type, severity and confidence |
| Settings | Settings: theme, high contrast, reduced motion, language |

### Phone — web build rendered at 390×844
<table>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/home-dark.webp"><img src="docs/screenshots/phone/home-light.webp" width="180" alt="Home with tests, streak and last score"></picture><br><sub><b>Home</b><br>Home with tests, streak and last score</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/onboarding-dark.webp"><img src="docs/screenshots/phone/onboarding-light.webp" width="180" alt="First-run onboarding: screen, don't diagnose"></picture><br><sub><b>Onboarding</b><br>First-run onboarding: screen, don't diagnose</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/tests-dark.webp"><img src="docs/screenshots/phone/tests-light.webp" width="180" alt="All tests plus the before-you-start checklist"></picture><br><sub><b>Tests</b><br>All tests plus the before-you-start checklist</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/quick-dark.webp"><img src="docs/screenshots/phone/quick-light.webp" width="180" alt="Quick screening plate with the on-screen keypad"></picture><br><sub><b>Quick screening</b><br>Quick screening plate with the on-screen keypad</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/arrangement-dark.webp"><img src="docs/screenshots/phone/arrangement-light.webp" width="180" alt="D-15 style hue arrangement with draggable caps"></picture><br><sub><b>Hue arrangement</b><br>D-15 style hue arrangement with draggable caps</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/contrast-dark.webp"><img src="docs/screenshots/phone/contrast-light.webp" width="180" alt="Adaptive arrow staircase contrast test"></picture><br><sub><b>Contrast sensitivity</b><br>Adaptive arrow staircase contrast test</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/lens-dark.webp"><img src="docs/screenshots/phone/lens-light.webp" width="180" alt="Live colour lens naming the colour under the crosshair"></picture><br><sub><b>Colour lens</b><br>Live colour lens naming the colour under the crosshair</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/simulator-dark.webp"><img src="docs/screenshots/phone/simulator-light.webp" width="180" alt="Photo shown as protan, deutan and tritan"></picture><br><sub><b>Photo simulator</b><br>Photo shown as protan, deutan and tritan</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/practice-dark.webp"><img src="docs/screenshots/phone/practice-light.webp" width="180" alt="60-second odd-hue practice game"></picture><br><sub><b>Daily practice</b><br>60-second odd-hue practice game</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/history-dark.webp"><img src="docs/screenshots/phone/history-light.webp" width="180" alt="History with CSV and JSON backup export"></picture><br><sub><b>History</b><br>History with CSV and JSON backup export</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/result-dark.webp"><img src="docs/screenshots/phone/result-light.webp" width="180" alt="Result report with type, severity and confidence"></picture><br><sub><b>Result report</b><br>Result report with type, severity and confidence</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/phone/settings-dark.webp"><img src="docs/screenshots/phone/settings-light.webp" width="180" alt="Settings: theme, high contrast, reduced motion, language"></picture><br><sub><b>Settings</b><br>Settings: theme, high contrast, reduced motion, language</sub></td></tr>
</table>

### Tablet — web build rendered at 834×1112
<table>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/home-dark.webp"><img src="docs/screenshots/tablet/home-light.webp" width="220" alt="Home with tests, streak and last score"></picture><br><sub><b>Home</b><br>Home with tests, streak and last score</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/onboarding-dark.webp"><img src="docs/screenshots/tablet/onboarding-light.webp" width="220" alt="First-run onboarding: screen, don't diagnose"></picture><br><sub><b>Onboarding</b><br>First-run onboarding: screen, don't diagnose</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/tests-dark.webp"><img src="docs/screenshots/tablet/tests-light.webp" width="220" alt="All tests plus the before-you-start checklist"></picture><br><sub><b>Tests</b><br>All tests plus the before-you-start checklist</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/quick-dark.webp"><img src="docs/screenshots/tablet/quick-light.webp" width="220" alt="Quick screening plate with the on-screen keypad"></picture><br><sub><b>Quick screening</b><br>Quick screening plate with the on-screen keypad</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/arrangement-dark.webp"><img src="docs/screenshots/tablet/arrangement-light.webp" width="220" alt="D-15 style hue arrangement with draggable caps"></picture><br><sub><b>Hue arrangement</b><br>D-15 style hue arrangement with draggable caps</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/contrast-dark.webp"><img src="docs/screenshots/tablet/contrast-light.webp" width="220" alt="Adaptive arrow staircase contrast test"></picture><br><sub><b>Contrast sensitivity</b><br>Adaptive arrow staircase contrast test</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/lens-dark.webp"><img src="docs/screenshots/tablet/lens-light.webp" width="220" alt="Live colour lens naming the colour under the crosshair"></picture><br><sub><b>Colour lens</b><br>Live colour lens naming the colour under the crosshair</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/simulator-dark.webp"><img src="docs/screenshots/tablet/simulator-light.webp" width="220" alt="Photo shown as protan, deutan and tritan"></picture><br><sub><b>Photo simulator</b><br>Photo shown as protan, deutan and tritan</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/practice-dark.webp"><img src="docs/screenshots/tablet/practice-light.webp" width="220" alt="60-second odd-hue practice game"></picture><br><sub><b>Daily practice</b><br>60-second odd-hue practice game</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/history-dark.webp"><img src="docs/screenshots/tablet/history-light.webp" width="220" alt="History with CSV and JSON backup export"></picture><br><sub><b>History</b><br>History with CSV and JSON backup export</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/result-dark.webp"><img src="docs/screenshots/tablet/result-light.webp" width="220" alt="Result report with type, severity and confidence"></picture><br><sub><b>Result report</b><br>Result report with type, severity and confidence</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/tablet/settings-dark.webp"><img src="docs/screenshots/tablet/settings-light.webp" width="220" alt="Settings: theme, high contrast, reduced motion, language"></picture><br><sub><b>Settings</b><br>Settings: theme, high contrast, reduced motion, language</sub></td></tr>
</table>

### Desktop — web build rendered at 1440×900
<table>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/home-dark.webp"><img src="docs/screenshots/desktop/home-light.webp" width="400" alt="Home with tests, streak and last score"></picture><br><sub><b>Home</b><br>Home with tests, streak and last score</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/onboarding-dark.webp"><img src="docs/screenshots/desktop/onboarding-light.webp" width="400" alt="First-run onboarding: screen, don't diagnose"></picture><br><sub><b>Onboarding</b><br>First-run onboarding: screen, don't diagnose</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/tests-dark.webp"><img src="docs/screenshots/desktop/tests-light.webp" width="400" alt="All tests plus the before-you-start checklist"></picture><br><sub><b>Tests</b><br>All tests plus the before-you-start checklist</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/quick-dark.webp"><img src="docs/screenshots/desktop/quick-light.webp" width="400" alt="Quick screening plate with the on-screen keypad"></picture><br><sub><b>Quick screening</b><br>Quick screening plate with the on-screen keypad</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/arrangement-dark.webp"><img src="docs/screenshots/desktop/arrangement-light.webp" width="400" alt="D-15 style hue arrangement with draggable caps"></picture><br><sub><b>Hue arrangement</b><br>D-15 style hue arrangement with draggable caps</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/contrast-dark.webp"><img src="docs/screenshots/desktop/contrast-light.webp" width="400" alt="Adaptive arrow staircase contrast test"></picture><br><sub><b>Contrast sensitivity</b><br>Adaptive arrow staircase contrast test</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/lens-dark.webp"><img src="docs/screenshots/desktop/lens-light.webp" width="400" alt="Live colour lens naming the colour under the crosshair"></picture><br><sub><b>Colour lens</b><br>Live colour lens naming the colour under the crosshair</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/simulator-dark.webp"><img src="docs/screenshots/desktop/simulator-light.webp" width="400" alt="Photo shown as protan, deutan and tritan"></picture><br><sub><b>Photo simulator</b><br>Photo shown as protan, deutan and tritan</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/practice-dark.webp"><img src="docs/screenshots/desktop/practice-light.webp" width="400" alt="60-second odd-hue practice game"></picture><br><sub><b>Daily practice</b><br>60-second odd-hue practice game</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/history-dark.webp"><img src="docs/screenshots/desktop/history-light.webp" width="400" alt="History with CSV and JSON backup export"></picture><br><sub><b>History</b><br>History with CSV and JSON backup export</sub></td></tr>
<tr><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/result-dark.webp"><img src="docs/screenshots/desktop/result-light.webp" width="400" alt="Result report with type, severity and confidence"></picture><br><sub><b>Result report</b><br>Result report with type, severity and confidence</sub></td><td align="center" valign="top"><picture><source media="(prefers-color-scheme: dark)" srcset="docs/screenshots/desktop/settings-dark.webp"><img src="docs/screenshots/desktop/settings-light.webp" width="400" alt="Settings: theme, high contrast, reduced motion, language"></picture><br><sub><b>Settings</b><br>Settings: theme, high contrast, reduced motion, language</sub></td></tr>
</table>

## Install

Download the file for your platform from the [latest release](https://github.com/junaaid96/color-vision-lab/releases/latest):

| Platform | File | Notes |
|---|---|---|
| Windows 10/11 | `.msi` or `-setup.exe` | Not code-signed yet, so SmartScreen may warn: More info → Run anyway. |
| macOS 11+ | `.dmg` (universal) | Not notarised yet: right-click the app → Open the first time. |
| Linux | `.AppImage`, `.deb`, `.rpm` | `chmod +x` the AppImage. |
| Android 7+ | `.apk` | Allow "install unknown apps" when asked. |
| iOS | build from source | Needs Xcode and your own signing team (see below). |
| Web | [website](https://color-vision-lab-codejborg.vercel.app) or `-web.zip` | Any static host. |

## Develop

Requirements: Node.js 22+, Rust stable, and the [Tauri prerequisites](https://v2.tauri.app/start/prerequisites/).

```sh
npm ci
npm run tauri dev        # desktop app with hot reload
npm run dev              # frontend only in a browser (uses web fallbacks)
npm run lint && npm run typecheck && npm test
(cd src-tauri && cargo test && cargo clippy --all-targets -- -D warnings)
```

## Build

### macOS
```sh
npm run tauri build                                       # .app and .dmg for this Mac
rustup target add x86_64-apple-darwin
npm run tauri build -- --target universal-apple-darwin    # universal binary
```

### Windows
Install the Visual Studio C++ Build Tools ("Desktop development with C++") and WebView2 (preinstalled on Windows 11), then:
```powershell
npm ci
npm run tauri build      # target\release\bundle\msi and \nsis
```

### Linux (Debian/Ubuntu)
```sh
sudo apt install libwebkit2gtk-4.1-dev build-essential curl wget file libxdo-dev libssl-dev libayatana-appindicator3-dev librsvg2-dev
npm ci
npm run tauri build      # .deb, .rpm and .AppImage in src-tauri/target/release/bundle
```

### Android
JDK 17, Android SDK (platform 36, build-tools 36) and NDK 28:
```sh
export ANDROID_HOME=~/Library/Android/sdk NDK_HOME=$ANDROID_HOME/ndk/28.2.13676358
rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android
npm run tauri android init
npm run tauri android build -- --debug --apk      # or: --apk --aab for release (signed if src-tauri/gen/android/keystore.properties exists)
```

### iOS
Needs macOS with full Xcode and CocoaPods:
```sh
rustup target add aarch64-apple-ios aarch64-apple-ios-sim
npm run tauri ios init
npm run tauri ios dev           # simulator
npm run tauri ios build         # needs an Apple development team for a device IPA
```

### Web
```sh
npm run build:web        # static site in dist/
```
The root `vercel.json` deploys this build to Vercel (build command `npm run build:web`, output `dist`). The marketing page in `landing/` is a separate static Vercel project.

### Releases
Pushing a `v*` tag runs `.github/workflows/release.yml`. It builds Windows, Linux, macOS (universal), Android (APK/AAB), an optional unsigned iOS build and the web zip, and attaches them to a draft GitHub Release. The Android APK and AAB are signed with the release keystore from the `ANDROID_KEYSTORE_*` / `ANDROID_KEY_*` repo secrets, and a final job writes `SHA256SUMS.txt` and publishes the release as Latest. Publish the draft so the desktop updater can find `latest.json`.
Updater artifacts are signed with a key from `tauri signer generate`. Add the private key and its password as the `TAURI_SIGNING_PRIVATE_KEY` and `TAURI_SIGNING_PRIVATE_KEY_PASSWORD` repository secrets; the public key is in `src-tauri/tauri.conf.json`.

## Architecture

```
src/                 React 19 + TypeScript (Vite, Tailwind 4)
  lib/               pure colour science and scoring (Vitest)
  platform/          one wrapper per Tauri plugin, each with a browser fallback
  i18n/              strings (en, bn, es, hi)
  screens/           one file per section
src-tauri/           Rust: plugin setup, filter_pixels (raw-byte IPC), app_info
  src/color.rs       Machado/daltonize matrices (cargo tests)
  capabilities/      least-privilege permissions (desktop, mobile)
landing/             static marketing page
```

The frontend detects Tauri at runtime (and `vite build --mode web` compiles the native paths out). Storage uses the store plugin or `localStorage`; saving uses dialog + fs or a browser download; the photo pipeline uses Rust over raw IPC (no JSON) or a JS loop. Live camera filters are SVG `feColorMatrix` filters in linear RGB, so they run on the GPU.

## Security model

- **No remote content.** The UI is bundled; there are no CDNs, fonts or analytics.
- **Strict CSP** in `tauri.conf.json` (`script-src 'self'`, no `eval`, `object-src 'none'`, `frame-ancestors 'none'`) and the same headers for the web build in `vercel.json`. `freezePrototype` is on.
- **Least privilege.** Capabilities grant only the commands the app uses (for example `fs:allow-write-file` and `fs:allow-write-text-file` with no default scope, so writes go only to paths the user picks in a save dialog; `opener:allow-open-url`; `process:allow-restart`). The updater, process, window-state and single-instance plugins are desktop-only; haptics are mobile-only.
- **Input checks.** `filter_pixels` validates the buffer size (multiple of 4, at most 64 MB), the mode and the severity.
- **Signed updates.** The updater only installs packages signed by the release key.
- **Privacy.** See [PRIVACY.md](PRIVACY.md).

## Tauri plugins

| Plugin | Used for | Platforms |
|---|---|---|
| store | settings, history, streak | all |
| fs | writing exported files | all |
| dialog | save/open dialogs, confirmations | all |
| updater | signed auto-updates from GitHub Releases | desktop |
| process | relaunch after an update | desktop |
| os | platform and architecture | all |
| opener | opening links in the browser | all |
| notification | "result saved" notifications (opt-in) | all |
| window-state | remembering window size and position | desktop |
| single-instance | focusing the running app (with deep links) | desktop |
| deep-link | `colorvisionlab://<section>` | all |
| haptics | feedback in tests and practice | mobile |
| clipboard-manager | copying a result summary | all |
| log | log files for troubleshooting | all |

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE) © 2026 Md. Junaidul Islam
