# Color Vision Lab

Private, offline colour vision screening and tools for **Windows, macOS, Linux, Android, iOS and the web**.
Built with [Tauri v2](https://v2.tauri.app), React and Rust.

[Website](https://color-vision-lab-codejborg.vercel.app) · [Releases](https://github.com/junaaid96/color-vision-lab/releases) · [Privacy](PRIVACY.md) · [Design notes](docs/DESIGN.md)

> Screening, not a diagnosis. Screens vary in colour and brightness. If you are concerned about your vision, see an eye-care professional.

<p align="center">
  <img src="docs/screenshots/home.png" alt="Home screen" width="62%">
  <img src="docs/screenshots/mobile.png" alt="Mobile, light theme" width="20%">
</p>
<p align="center">
  <img src="docs/screenshots/quick.png" alt="Quick screening plate" width="41%">
  <img src="docs/screenshots/arrangement.png" alt="Hue arrangement test" width="41%">
</p>

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
npm run tauri android build -- --debug --apk      # or: --apk --aab for release (sign with your keystore)
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
Pushing a `v*` tag runs `.github/workflows/release.yml`. It builds Windows, Linux, macOS (universal), Android (APK/AAB), an optional unsigned iOS build and the web zip, and attaches them to a draft GitHub Release. Publish the draft so the desktop updater can find `latest.json`.
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
