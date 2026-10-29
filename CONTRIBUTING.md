# Contributing

Thanks for helping make colour vision tools better.

## Setup
- Node.js 22+ and npm.
- Rust (stable) via [rustup](https://rustup.rs) and the [Tauri prerequisites](https://v2.tauri.app/start/prerequisites/) for your OS.
- Android: JDK 17, Android SDK (platform 36, build-tools 36), NDK 28, and `rustup target add aarch64-linux-android armv7-linux-androideabi i686-linux-android x86_64-linux-android`.
- iOS: macOS with full Xcode, CocoaPods, and `rustup target add aarch64-apple-ios aarch64-apple-ios-sim`.

```sh
npm ci
npm run tauri dev     # desktop app with hot reload
npm run dev           # browser only (web fallbacks)
```

## Before you open a PR
These must pass (CI runs the same):

```sh
npm run lint && npm run typecheck && npm test && npm run build:web
cd src-tauri && cargo fmt --check && cargo clippy --all-targets -- -D warnings && cargo test
```

## Guidelines
- Keep features offline and tracking-free. New network access needs a very good reason and a PRIVACY.md update.
- Colour science and scoring live in pure TypeScript in `src/lib/` with Vitest tests. Pixel work that also runs natively lives in `src-tauri/src/color.rs` with Rust tests. Keep both matrix sets identical.
- Platform access goes through `src/platform/index.ts`, which must always have a browser fallback.
- New Tauri commands or plugins need the narrowest permission in `src-tauri/capabilities/`.
- Every user-visible string goes in `src/i18n/en.ts`. Add `bn`, `es` and `hi` translations in `src/i18n/index.ts` when you can (English is the fallback; mention it in the PR).
- Accessibility: label non-text controls, keep 44px touch targets, support the keyboard, and never rely on colour alone.
- Do not present results as a diagnosis.

## Translations
Add a locale to `Locale` in `src/lib/models.ts`, a partial string table in `src/i18n/index.ts`, and an entry in `LOCALES`.
