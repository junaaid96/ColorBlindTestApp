import { useEffect, useState } from "react";
import { useApp } from "../state";
import { Icon, Page } from "../components/ui";
import { applyMatrixToPixels, correctionMatrix, simulationMatrix, CVD_TYPES, type CvdType } from "../lib/color";
import { nativeFilterPixels, saveFile } from "../platform";

type Panel = { key: "none" | CvdType; url: string };
const MAX = 1600;

async function loadPixels(file: Blob) {
  const bmp = await createImageBitmap(file);
  const scale = Math.min(1, MAX / Math.max(bmp.width, bmp.height));
  const c = new OffscreenCanvas(Math.round(bmp.width * scale), Math.round(bmp.height * scale));
  const ctx = c.getContext("2d")!;
  ctx.drawImage(bmp, 0, 0, c.width, c.height);
  return ctx.getImageData(0, 0, c.width, c.height);
}
async function toUrl(data: ImageData) {
  const c = new OffscreenCanvas(data.width, data.height);
  c.getContext("2d")!.putImageData(data, 0, 0);
  return URL.createObjectURL(await c.convertToBlob({ type: "image/png" }));
}

/** Photo simulator: original + protan/deutan/tritan side by side. Pixels go through Rust in the app, JS on the web. */
export default function Simulator() {
  const { t } = useApp();
  const [src, setSrc] = useState<ImageData | null>(null);
  const [severity, setSeverity] = useState(1);
  const [correct, setCorrect] = useState(false);
  const [panels, setPanels] = useState<Panel[]>([]);
  const [engine, setEngine] = useState<"rust" | "js" | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (!src) return;
    let cancelled = false;
    const urls: string[] = [];
    setBusy(true);
    (async () => {
      const out: Panel[] = [{ key: "none", url: await toUrl(src) }];
      let used: "rust" | "js" = "js";
      for (const ty of CVD_TYPES) {
        const mode = `${ty}-${correct ? "fix" : "sim"}`;
        let px: Uint8ClampedArray<ArrayBuffer>;
        const native = await nativeFilterPixels(src.data, mode, severity).catch(() => null);
        if (native) { px = new Uint8ClampedArray(native); used = "rust"; }
        else { px = new Uint8ClampedArray(src.data); applyMatrixToPixels(px, correct ? correctionMatrix(ty, severity) : simulationMatrix(ty, severity)); }
        out.push({ key: ty, url: await toUrl(new ImageData(px, src.width, src.height)) });
      }
      out.forEach((p) => urls.push(p.url));
      if (!cancelled) { setPanels(out); setEngine(used); setBusy(false); }
    })();
    return () => { cancelled = true; urls.forEach(URL.revokeObjectURL); };
  }, [src, severity, correct]);

  const onFile = async (f?: File | null) => { if (f && f.type.startsWith("image/")) setSrc(await loadPixels(f)); };
  const savePanel = async (p: Panel) => {
    const bytes = new Uint8Array(await (await fetch(p.url)).arrayBuffer());
    await saveFile(`color-vision-${p.key}${correct ? "-enhanced" : ""}.png`, bytes, "image/png");
  };

  return (
    <Page title={t("sim_title")} subtitle={t("sim_desc")}>
      <div className="card mb-6 flex flex-wrap items-center gap-4 p-5" onDragOver={(e) => e.preventDefault()} onDrop={(e) => { e.preventDefault(); onFile(e.dataTransfer.files[0]); }}>
        <label className="btn btn-primary cursor-pointer"><Icon name="simulator" size={18} />{t("sim_pick")}<input type="file" accept="image/*" className="sr-only" onChange={(e) => onFile(e.target.files?.[0])} /></label>
        <span className="muted text-sm">{t("sim_drop")}</span>
        <label className="ml-auto min-w-48 text-sm">{t("filter_severity", { n: Math.round(severity * 100) })}<input type="range" min={0} max={1} step={0.05} value={severity} onChange={(e) => setSeverity(+e.target.value)} /></label>
        <button className="chip" aria-pressed={correct} onClick={() => setCorrect(!correct)}>{t("filter_correct")}</button>
        {engine && <span className="muted text-xs" aria-live="polite">{busy ? "…" : t(engine === "rust" ? "sim_native" : "sim_js")}</span>}
      </div>
      {panels.length > 0 && (
        <div className="grid gap-4 sm:grid-cols-2">
          {panels.map((p) => (
            <figure key={p.key} className="card overflow-hidden">
              <img src={p.url} alt={t(p.key === "none" ? "type_none" : `type_${p.key}`)} className="w-full" />
              <figcaption className="flex items-center justify-between p-3 text-sm font-semibold">{t(p.key === "none" ? "type_none" : `type_${p.key}`)}
                <button className="btn !px-3 text-xs" onClick={() => savePanel(p)} aria-label={t("sim_save")}><Icon name="download" size={16} /></button></figcaption>
            </figure>
          ))}
        </div>
      )}
    </Page>
  );
}
