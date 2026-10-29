# Color Vision Lab — landing page

Static download page for Color Vision Lab (plain HTML, CSS and JS, no build step). Live: https://color-vision-lab-codejborg.vercel.app

## Preview locally
```sh
cd landing
python3 -m http.server 8000
# open http://localhost:8000
```

## Deploy on Vercel
1. Import this repo in Vercel.
2. Root Directory: `landing`
3. Framework Preset: Other
4. Build Command: none (leave empty), Output Directory: default
5. Deploy.

When a new APK is released, update the download link and SHA-256 in `index.html`.

## Structure (v3 redesign)
- `index.html`, `style.css`, `app.js`: the page (no framework, no build). `app.js` detects the visitor's OS for the main download button and runs the Machado 2009 simulator demo.
- `img/`: screenshots (WebP) copied from `docs/screenshots`.
- `app/`: the static web build of the app, unpacked from the release's `-web.zip` (`npm run build:web` output). Replace it on each release.
- `og.png`: social preview image (1200×630).

When a new version is released, update the asset links, sizes and version in `index.html` and `app.js`, and refresh `app/`.
