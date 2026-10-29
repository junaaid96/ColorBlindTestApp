# Privacy

Color Vision Lab works offline and keeps everything on your device.

- **No accounts, ads, analytics, crash reporting or tracking SDKs.**
- **Network:** the only network request is the optional desktop update check (Settings → Check for updates). It downloads `latest.json` from this repository's GitHub Releases, and updates are verified with a signature before they are installed. Mobile and web builds make no network calls of their own. The web build is a static site that caches itself for offline use.
- **Camera** (optional) is used only by Color Lens. Frames are analysed in memory and are never saved or sent anywhere.
- **Photos** you open in the Simulator or Color Lens are processed in memory. They are not copied or uploaded.
- **Results** (history, streak, settings) are stored locally: in the app's private store file (`color-vision-lab.json`) in native builds, and in `localStorage` in the web build. You can export or clear them in History, or remove them by uninstalling or clearing site data.
- **Files** are written only where you choose in a save dialog (reports, CSV/JSON exports, images).
- **Sharing** happens only when you tap Share, and you pick the destination.
