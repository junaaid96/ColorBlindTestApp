# Color Vision Lab — landing page

Static download page for the Color Vision Lab Android app (plain HTML, CSS and JS, no build step). Live: https://color-vision-lab.vercel.app

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
