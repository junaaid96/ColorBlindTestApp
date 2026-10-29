# Color Vision Lab

A modern, offline, privacy-first Android app to **screen** your color vision, see the world through other eyes, and identify colors with your camera. It was rebuilt from scratch in Kotlin and Jetpack Compose (Material 3). It replaces the original Java Ishihara quiz.

> ⚠️ **Screening only, not a medical diagnosis.** Phone screens vary in color and brightness. If you have concerns, see an eye-care professional.

## Features
| | |
|---|---|
| **Procedural Ishihara-style plates** | Plates are generated fresh on the device from confusion-line colors (Machado 2009 model), with lightness noise so luminance gives no cue. Quick (7) and Full (16) modes include control, red-green, tritan and protan/deutan diagnostic plates. |
| **Result report** | Type, severity, confidence, a per-plate breakdown, share as text or **PDF**. |
| **Hue arrangement (D-15 style)** | Tap-to-order 15 caps, scored with Vingrys & King-Smith moments: confusion index, selectivity, crossings and likely axis. |
| **Contrast sensitivity** | A 1-up/2-down adaptive staircase on a directional arrow, giving log CS. |
| **Color Lens** | Live CameraX view that names the color under the crosshair. It can simulate protan/deutan/tritan or *enhance* (daltonize) in real time, with freeze, text-to-speech and haptics. |
| **Photo simulator** | Pick any photo and compare original, protan, deutan and tritan side by side, with a severity slider and daltonize toggle. |
| **History & trends** | Results stored on the device with a trend chart for each test. |
| **Daily practice** | A 60-second odd-hue game with levels, lives, best score and a daily streak. |
| **Accessibility** | Okabe–Ito color-blind-safe palette. Icons always paired with color. TalkBack labels and live regions. High-contrast theme, reduced motion, 48dp targets. |
| **Adaptive** | Phone, tablet and foldable layouts (navigation bar, rail or drawer). Light, dark, dynamic color, splash screen, themed adaptive icon. |
| **Languages** | English, বাংলা, Español, हिन्दी (per-app language). |
| **Private** | No internet permission, no accounts, no analytics. See [PRIVACY.md](PRIVACY.md). |

## Tech
Kotlin 2.4 · AGP 9.5 · Gradle 9.8 · compile/target SDK 37 · min SDK 26 · Compose BOM 2026.09 · Material 3 · Navigation Compose · ViewModel + StateFlow · DataStore · CameraX · coroutines · Kotlin DSL + version catalog · manual DI.

```
app/src/main/java/io/github/junaaid96/colorvision/
  color/    color science: CVD simulation, daltonize, CIELAB, color naming
  tests/    plate generator, scoring, D-15, staircase, practice
  data/     models + DataStore repository
  ui/       theme, navigation, shared components
  feature/  screens
```

## Build
```bash
./gradlew assembleDebug testDebugUnitTest lint
```
CI (GitHub Actions) builds, tests, lints and uploads the debug APK on every push and pull request.

Design research and rationale: [docs/DESIGN.md](docs/DESIGN.md). Contributing: [CONTRIBUTING.md](CONTRIBUTING.md). License: [MIT](LICENSE).
