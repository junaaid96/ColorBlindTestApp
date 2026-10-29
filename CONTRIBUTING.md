# Contributing

Thanks for helping make color vision tools better.

## Setup
- JDK 17, Android SDK platform 37 and build-tools 36.
- `./gradlew assembleDebug testDebugUnitTest lint` must pass before you open a PR (CI runs the same).

## Guidelines
- Kotlin + Jetpack Compose + Material 3 only; keep features offline and tracking-free.
- Put color science and scoring in pure Kotlin (`color/`, `tests/`) with unit tests; UI in `feature/`.
- Every user-visible string goes in `res/values/strings.xml` **and** the `bn`, `es` and `hi` translations (English fallback is fine; mark it in the PR).
- Accessibility: content descriptions for non-text UI, 48dp touch targets, never rely on color alone.
- Do not present results as a diagnosis.

## Translations
Add `res/values-<lang>/strings.xml`; the locale config is generated automatically, so the language appears in the system per-app language settings.
