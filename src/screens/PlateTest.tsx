import { useEffect, useRef, useState } from "react";
import { useApp } from "../state";
import { Announce, Page, PlateView } from "../components/ui";
import { planPlates } from "../lib/plates";
import { scorePlates } from "../lib/scoring";
import { notify } from "../platform";
import { Calibration } from "./Home";

export default function PlateTest({ full }: { full: boolean }) {
  const { t, addResult, showResult, buzz, settings, go } = useApp();
  const [plates] = useState(() => planPlates(full, (Date.now() % 100000) + 1));
  const [i, setI] = useState(0);
  const [answers, setAnswers] = useState<string[]>([]);
  const [value, setValue] = useState("");
  const [calibrated, setCalibrated] = useState(settings.calibrated);
  const input = useRef<HTMLInputElement>(null);
  // Only autofocus with a mouse/keyboard; on touch it would pop the system keyboard over the on-screen keypad.
  useEffect(() => { if (matchMedia("(pointer: fine)").matches) input.current?.focus({ preventScroll: true }); }, [i, calibrated]);

  if (!calibrated) return <Page title={t(full ? "full_title" : "quick_title")}><Calibration onDone={() => setCalibrated(true)} /></Page>;

  const submit = (v: string) => {
    buzz("tap");
    const next = [...answers, v];
    setValue("");
    if (next.length < plates.length) { setAnswers(next); setI(i + 1); return; }
    const r = scorePlates(plates, next);
    const entry = addResult({ kind: full ? "full" : "quick", classification: r.classification, severity: r.severity, score: r.score, confidence: Math.round(r.confidence * 100), details: r.outcomes });
    buzz("success");
    if (settings.notifications) notify(t("result_title"), t(`cls_${r.classification}`));
    showResult(entry);
  };
  const press = (k: string) => setValue((v) => (k === "⌫" ? v.slice(0, -1) : (v + k).slice(0, 2)));

  return (
    <Page title={t(full ? "full_title" : "quick_title")} subtitle={t("plate_of", { n: i + 1, total: plates.length })} actions={<button className="btn" onClick={() => go("tests")}>{t("cancel")}</button>}>
      <div className="h-1.5 w-full overflow-hidden rounded-full bg-[var(--line)]" role="progressbar" aria-valuemin={0} aria-valuemax={plates.length} aria-valuenow={i}>
        <div className="h-full bg-brand transition-all" style={{ width: `${(i / plates.length) * 100}%` }} />
      </div>
      <Announce text={t("plate_of", { n: i + 1, total: plates.length })} />
      <div className="mt-6 grid grid-cols-1 items-center gap-8 md:grid-cols-[minmax(0,1fr)_20rem]">
        <div className="grid min-w-0 place-items-center"><PlateView key={i} plate={plates[i]} /></div>
        <form className="card flex min-w-0 flex-col gap-4 p-5" onSubmit={(e) => { e.preventDefault(); submit(value); }}>
          <label htmlFor="ans" className="font-semibold">{t("plate_prompt")}</label>
          <input id="ans" ref={input} inputMode="numeric" pattern="[0-9]*" autoComplete="off" maxLength={2} value={value}
            onChange={(e) => setValue(e.target.value.replace(/\D/g, "").slice(0, 2))}
            className="w-full min-w-0 rounded-2xl border border-[var(--line)] bg-transparent px-4 py-3 text-center text-3xl font-bold tracking-widest" />
          <div className="grid grid-cols-3 gap-2">
            {["1", "2", "3", "4", "5", "6", "7", "8", "9", "⌫", "0"].map((k) => <button type="button" key={k} className="btn !rounded-2xl" onClick={() => press(k)} aria-label={k === "⌫" ? "Delete" : k}>{k}</button>)}
            <button type="submit" className="btn btn-primary !rounded-2xl" disabled={!value}>{t("next")}</button>
          </div>
          <button type="button" className="btn" onClick={() => submit("")}>{t("plate_nothing")}</button>
          <p className="muted text-xs">{t("plate_hint")}</p>
        </form>
      </div>
    </Page>
  );
}
