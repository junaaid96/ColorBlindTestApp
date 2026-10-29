# Design notes & research (2026)

## Disclaimer
This app is an educational screening tool. Smartphone displays are not calibrated test media, so no result here is a diagnosis. Every result screen, the onboarding and the About section say so.

## Research summary
**Clinical tests we emulate**
- **Ishihara pseudo-isochromatic plates.** These are the standard red-green screen: vanishing, transformation and hidden-digit designs, plus a control plate. Printed plates fade and can be memorised. We generate plates procedurally for each session from a seed, so there is nothing to memorise, and a missed control plate marks the session *inconclusive*.
- **Farnsworth D-15 / FM100.** Arrangement tests classify by axis. We use the Vingrys & King-Smith (1988) moment-of-inertia method in CIELAB. It gives a confusion index (C), a selectivity index (S) and the major-axis angle, which is compared with the protan, deutan and tritan axes. Those axes are derived from our own simulator, so the scorer and the simulator stay consistent. A 2025 smartphone D-15 study reported high sensitivity and specificity on one controlled device, with maximum brightness and colour-shifting features disabled. That is why onboarding has calibration tips.
- **Contrast sensitivity.** Pelli-Robson-style letter charts are replaced by a 4-direction arrow and an adaptive 1-up/2-down staircase, which converges near 71% correct. The reported threshold is the geometric mean of the last 4 reversals.

**Color science**
- Simulation uses **Machado, Oliveira & Fernandes (2009)** matrices with severity interpolation in *linear* sRGB. This is the approach open tools such as Metamer, DaltonLens and Colorblindly use. Tritan uses the Machado matrix for simplicity; Brettel 1997 is more accurate and is a candidate upgrade.
- Daltonization uses the Fidaner-style error redistribution approach. The live camera uses a gamma-space 4x5 ColorMatrix approximation, which is fast enough for every frame.
- Plate figure colors are found by search. The figure must be clearly distinct from the background under normal vision (ΔE CIE76), nearly identical under the target simulation, and close in luminance. Random lightness noise per dot then removes any brightness cue.
- Color naming matches the nearest of about 50 named colors in CIELAB, with a confidence value.

**Product landscape (2026)**
Apps such as Tembrica, RGBlind, Color Blind Pal, Chromatic Vision Simulator and Enchroma's test offer either testing *or* assistance. Few combine both, few are fully offline, and most rely on static plate images. Common gaps are no progress tracking, ads and trackers, poor TalkBack support and no localisation. Bengali and Hindi are rarely offered.

**UI/UX direction**
- Material 3 (stable 1.4) with a custom expressive type scale and large rounded cards. M3 Expressive components were still beta in material3 1.5, so they were left out to keep the build on stable releases.
- **Never rely on color alone.** Every state pairs an icon with text. The brand palette is Okabe–Ito.
- Adaptive navigation through `NavigationSuiteScaffold`: a bottom bar on phones, a rail or drawer on tablets and foldables, a max content width of 840dp, and a side-by-side plate and keypad when the screen is wide.
- Gentle gamification without dark patterns: a daily streak, best level and a 60-second session. There are no notifications or ads.
- Privacy is a feature: no `INTERNET` permission at all.

## Architecture
A single activity with Compose and Navigation Compose. `AppViewModel` exposes StateFlows from a DataStore `Repository`, using manual DI through `ColorVisionApp`. All color science and scoring is pure Kotlin with JVM unit tests.

## Next ideas
- A Brettel tritan model, a CIEDE2000 namer and localised color names.
- Per-device calibration (white point and gamma check).
- A Wear OS / glance widget for streaks, and a Quick Settings tile for the lens.
- An accessibility checker for UI screenshots (a palette contrast audit).
- A Cambridge Colour Test-style trivector test using Landolt-C targets.
- Export and import of history as CSV.
