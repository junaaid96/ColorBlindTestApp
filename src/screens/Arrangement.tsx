import { useState } from "react";
import { useApp } from "../state";
import { Page, Stat } from "../components/ui";
import { CAPS, caps, scoreArrangement, type ArrangementResult } from "../lib/hue";
import { hex } from "../lib/color";
import { Rng } from "../lib/random";

const COLORS = caps().map(hex);
function shuffled(): number[] {
  const rnd = new Rng(Date.now() % 1e9), a = Array.from({ length: CAPS }, (_, i) => i + 1);
  for (let i = a.length - 1; i > 0; i--) { const j = rnd.int(0, i + 1); [a[i], a[j]] = [a[j], a[i]]; }
  return a;
}

/** Tap-to-place D-15: tap caps from the tray in order; drag or keyboard to reorder the row. */
export default function Arrangement() {
  const { t, addResult, buzz } = useApp();
  const [tray, setTray] = useState<number[]>(shuffled);
  const [row, setRow] = useState<number[]>([]);
  const [sel, setSel] = useState<number | null>(null);
  const [drag, setDrag] = useState<number | null>(null);
  const [result, setResult] = useState<ArrangementResult | null>(null);

  const place = (c: number) => { buzz("tap"); setTray((tr) => tr.filter((x) => x !== c)); setRow((r) => [...r, c]); };
  const unplace = (c: number) => { setRow((r) => r.filter((x) => x !== c)); setTray((tr) => [...tr, c]); };
  const move = (from: number, to: number) => setRow((r) => { if (to < 0 || to >= r.length) return r; const n = [...r]; const [x] = n.splice(from, 1); n.splice(to, 0, x); return n; });
  const finish = () => {
    const r = scoreArrangement(row); setResult(r); buzz(r.passed ? "success" : "error");
    addResult({ kind: "arrangement", classification: r.passed ? "typical" : r.likelyAxis === "tritan" ? "tritan" : r.likelyAxis ?? "redGreen", severity: null, score: Math.round(r.cIndex * 100), confidence: Math.round(Math.min(1, CAPS / 15) * 70), details: { angle: +r.angle.toFixed(1), cIndex: +r.cIndex.toFixed(2), sIndex: +r.sIndex.toFixed(2), crossings: r.crossings, likelyAxis: r.likelyAxis, order: row.join("-") } });
  };
  const reset = () => { setTray(shuffled()); setRow([]); setResult(null); };
  const cap = (_c: number, extra = "") => `h-12 w-12 shrink-0 rounded-full border-2 border-white/60 shadow ${extra}`;

  return (
    <Page title={t("arr_title")} subtitle={t("arr_help")} actions={<button className="btn" onClick={reset}>{t("retry")}</button>}>
      <div className="card p-5">
        <ol className="flex min-h-16 flex-wrap items-center gap-2" aria-label="Arrangement">
          <li><span className={cap(0, "ring-2 ring-[var(--fg)]")} style={{ background: COLORS[0], display: "block" }} title="Pilot" /></li>
          {row.map((c, i) => (
            <li key={c} draggable onDragStart={() => setDrag(i)} onDragOver={(e) => e.preventDefault()} onDrop={() => { if (drag !== null) move(drag, i); setDrag(null); }}>
              <button className={cap(c, sel === i ? "ring-4 ring-warn" : "")} style={{ background: COLORS[c] }} aria-label={`Cap ${i + 1}. Arrow keys move, Delete returns it.`}
                onClick={() => setSel(sel === i ? null : i)}
                onKeyDown={(e) => { if (e.key === "ArrowLeft" || e.key === "ArrowRight") { e.preventDefault(); const to = i + (e.key === "ArrowLeft" ? -1 : 1); move(i, to); setSel(to); } if (e.key === "Delete" || e.key === "Backspace") unplace(c); }}
                onDoubleClick={() => unplace(c)} />
            </li>
          ))}
        </ol>
      </div>
      {tray.length > 0 && (
        <div className="card mt-4 p-5">
          <ul className="flex flex-wrap gap-3" aria-label="Unplaced caps">{tray.map((c) => <li key={c}><button className={cap(c)} style={{ background: COLORS[c] }} aria-label="Place cap" onClick={() => place(c)} /></li>)}</ul>
        </div>
      )}
      {!tray.length && !result && <button className="btn btn-primary mt-4" onClick={finish}>{t("done")}</button>}
      {result && (
        <div className="mt-6 grid grid-cols-2 gap-3 md:grid-cols-4">
          <Stat label={t("arr_cindex")} value={result.cIndex.toFixed(2)} tone={result.passed ? "ok" : "bad"} />
          <Stat label={t("arr_sindex")} value={result.sIndex.toFixed(2)} />
          <Stat label={t("arr_crossings")} value={result.crossings} />
          <Stat label={t("arr_axis")} value={result.likelyAxis ? t(`type_${result.likelyAxis}`) : "—"} />
          <p className="col-span-full font-semibold">{result.passed ? t("arr_pass") : t("arr_fail")}</p>
        </div>
      )}
    </Page>
  );
}
