import { useCallback, useEffect, useState } from "react";
import { useApp } from "../state";
import { Page, Stat } from "../components/ui";
import { ContrastStaircase } from "../lib/staircase";

type Dir = "up" | "down" | "left" | "right";
const DIRS: Dir[] = ["up", "right", "down", "left"];
const ROT = { up: 0, right: 90, down: 180, left: 270 };
const randDir = () => DIRS[Math.floor(Math.random() * 4)];

export default function Contrast() {
  const { t, addResult, buzz } = useApp();
  const [sc, setSc] = useState(() => new ContrastStaircase());
  const [dir, setDir] = useState<Dir>(randDir);
  const [contrast, setContrast] = useState(0.4);
  const [trials, setTrials] = useState(0);
  const [done, setDone] = useState<{ log: number; threshold: number } | null>(null);

  const answer = useCallback((d: Dir) => {
    if (done !== null) return;
    const s = sc; s.answer(d === dir); buzz(d === dir ? "tap" : "error"); setTrials(s.trials);
    if (s.finished) {
      const log = s.logSensitivity(); setDone({ log, threshold: s.threshold() });
      addResult({ kind: "contrast", classification: null, severity: null, score: Math.round(log * 100), confidence: Math.round(Math.min(1, s.reversalCount / 6) * 100), details: { threshold: +s.threshold().toFixed(4), trials: s.trials } });
    } else { setContrast(s.contrast); setDir(randDir()); }
  }, [sc, dir, done, addResult, buzz]);

  useEffect(() => {
    const k = (e: KeyboardEvent) => { const m: Record<string, Dir> = { ArrowUp: "up", ArrowDown: "down", ArrowLeft: "left", ArrowRight: "right" }; if (m[e.key]) { e.preventDefault(); answer(m[e.key]); } };
    addEventListener("keydown", k); return () => removeEventListener("keydown", k);
  }, [answer]);

  // Weber contrast on mid grey: arrow luminance = bg * (1 - c).
  const bg = 0.5, fg = bg * (1 - contrast);
  const toCss = (l: number) => { const v = Math.round((l <= 0.0031308 ? l * 12.92 : 1.055 * l ** (1 / 2.4) - 0.055) * 255); return `rgb(${v},${v},${v})`; };
  return (
    <Page title={t("contrast_title")} subtitle={t("contrast_help")} actions={done !== null ? <button className="btn" onClick={() => { const n = new ContrastStaircase(); setSc(n); setContrast(n.contrast); setTrials(0); setDone(null); }}>{t("retry")}</button> : undefined}>
      {done === null ? (
        <div className="grid place-items-center gap-6">
          <div className="grid aspect-square w-full max-w-sm place-items-center rounded-3xl" style={{ background: toCss(bg) }} role="img" aria-label="Low contrast arrow">
            <svg viewBox="0 0 100 100" className="w-1/2" style={{ transform: `rotate(${ROT[dir]}deg)` }}><path d="M50 10 L85 55 H62 V90 H38 V55 H15 Z" fill={toCss(fg)} /></svg>
          </div>
          <div className="grid grid-cols-3 gap-2">
            <span /><button className="btn" onClick={() => answer("up")} aria-label="Up">↑</button><span />
            <button className="btn" onClick={() => answer("left")} aria-label="Left">←</button><span className="muted grid place-items-center text-xs">{trials}</span><button className="btn" onClick={() => answer("right")} aria-label="Right">→</button>
            <span /><button className="btn" onClick={() => answer("down")} aria-label="Down">↓</button><span />
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-2 gap-3"><Stat label={t("contrast_logcs")} value={done.log.toFixed(2)} tone={done.log >= 1.6 ? "ok" : done.log >= 1.3 ? "warn" : "bad"} /><Stat label="Threshold" value={`${(done.threshold * 100).toFixed(2)}%`} /></div>
      )}
    </Page>
  );
}
