import { useEffect, useRef, useState } from "react";
import { useApp } from "../state";
import { CvdFilterDefs, FilterControls, filterId, Icon, Page, type ViewFilter } from "../components/ui";
import { hex, rgb } from "../lib/color";
import { nameColor, type Named } from "../lib/namer";
import { speak } from "../platform";

/** Live camera (getUserMedia) or a still photo; names the colour under the crosshair. */
export default function Lens() {
  const { t, settings, buzz } = useApp();
  const video = useRef<HTMLVideoElement>(null);
  const img = useRef<HTMLImageElement>(null);
  const canvas = useRef<HTMLCanvasElement>(null);
  const [stream, setStream] = useState<MediaStream | null>(null);
  const [photo, setPhoto] = useState<string | null>(null);
  const [frozen, setFrozen] = useState(false);
  const [error, setError] = useState("");
  const [named, setNamed] = useState<Named | null>(null);
  const [filter, setFilter] = useState<ViewFilter>({ type: null, correct: false, severity: 1 });
  const lastSpoken = useRef("");

  const start = async () => {
    setError("");
    try {
      const s = await navigator.mediaDevices.getUserMedia({ video: { facingMode: "environment", width: { ideal: 1280 } }, audio: false });
      setPhoto(null); setStream(s);
    } catch { setError(t("lens_denied")); }
  };
  const stop = () => { stream?.getTracks().forEach((tr) => tr.stop()); setStream(null); };
  useEffect(() => () => stream?.getTracks().forEach((tr) => tr.stop()), [stream]);
  useEffect(() => { if (video.current && stream) { video.current.srcObject = stream; video.current.play().catch(() => {}); } }, [stream]);
  useEffect(() => { if (frozen) video.current?.pause(); else video.current?.play().catch(() => {}); }, [frozen]);

  // Sample a 9x9 average at the centre a few times per second.
  useEffect(() => {
    const id = setInterval(() => {
      const src = stream ? video.current : photo ? img.current : null;
      const c = canvas.current;
      if (!src || !c) return;
      const w = src instanceof HTMLVideoElement ? src.videoWidth : src.naturalWidth, h = src instanceof HTMLVideoElement ? src.videoHeight : src.naturalHeight;
      if (!w || !h) return;
      const ctx = c.getContext("2d", { willReadFrequently: true })!;
      ctx.drawImage(src, w / 2 - 4, h / 2 - 4, 9, 9, 0, 0, 9, 9);
      const d = ctx.getImageData(0, 0, 9, 9).data;
      let r = 0, g = 0, b = 0;
      for (let i = 0; i < d.length; i += 4) { r += d[i]; g += d[i + 1]; b += d[i + 2]; }
      const n = d.length / 4;
      const res = nameColor(rgb(r / n / 255, g / n / 255, b / n / 255));
      setNamed(res);
      if (settings.speech && res.name !== lastSpoken.current) { lastSpoken.current = res.name; speak(res.name, settings.locale); buzz("tap"); }
    }, frozen ? 1000 : 300);
    return () => clearInterval(id);
  }, [stream, photo, frozen, settings.speech, settings.locale, buzz]);

  const fid = filterId(filter);
  const mediaStyle = fid ? { filter: `url(#${fid})` } : undefined;
  return (
    <Page title={t("lens_title")} subtitle={t("lens_desc")} actions={<>
      {stream ? <><button className="btn" onClick={() => setFrozen(!frozen)}>{frozen ? t("lens_unfreeze") : t("lens_freeze")}</button><button className="btn" onClick={stop}>{t("lens_stop")}</button></>
        : <button className="btn btn-primary" onClick={start}><Icon name="camera" size={18} />{t("lens_start")}</button>}
      <label className="btn cursor-pointer">{t("lens_pick")}<input type="file" accept="image/*" capture="environment" className="sr-only" onChange={(e) => { const f = e.target.files?.[0]; if (f) { stop(); setPhoto(URL.createObjectURL(f)); } }} /></label>
    </>}>
      <CvdFilterDefs severity={filter.severity} />
      {error && <p role="alert" className="text-bad mb-3">{error}</p>}
      <div className="grid gap-6 lg:grid-cols-[1fr_20rem]">
        <div className="relative grid aspect-[4/3] place-items-center overflow-hidden rounded-3xl bg-black">
          {stream && <video ref={video} playsInline muted className="h-full w-full object-cover" style={mediaStyle} />}
          {photo && <img ref={img} src={photo} alt="" className="h-full w-full object-contain" style={mediaStyle} />}
          {!stream && !photo && <Icon name="lens" size={64} />}
          <div className="pointer-events-none absolute inset-0 grid place-items-center" aria-hidden><div className="h-10 w-10 rounded-full border-2 border-white shadow-[0_0_0_2px_black]" /></div>
        </div>
        <div className="card flex flex-col gap-5 p-5">
          <div aria-live="polite" className="flex items-center gap-4">
            <span className="h-16 w-16 rounded-2xl border border-[var(--line)]" style={{ background: named ? hex(named.rgb) : "transparent" }} />
            <div><div className="text-2xl font-bold">{named?.name ?? "—"}</div>{named && <div className="muted font-mono text-sm">{hex(named.rgb)} · {Math.round(named.confidence * 100)}%</div>}</div>
          </div>
          <FilterControls value={filter} onChange={setFilter} />
        </div>
      </div>
      <canvas ref={canvas} width={9} height={9} hidden />
    </Page>
  );
}
