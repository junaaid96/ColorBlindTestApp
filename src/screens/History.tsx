import { useState } from "react";
import { useApp } from "../state";
import { Icon, Page } from "../components/ui";
import type { TestKind } from "../lib/models";
import { historyToCsv, historyToJson, parseHistoryJson } from "../lib/report";
import { askConfirm, pickTextFile, saveFile } from "../platform";

const KINDS: TestKind[] = ["quick", "full", "arrangement", "contrast", "practice"];
const TITLE = { quick: "quick_title", full: "full_title", arrangement: "arr_title", contrast: "contrast_title", practice: "practice_title" } as const;
const COLORS = { quick: "#0072B2", full: "#009E73", arrangement: "#CC79A7", contrast: "#E69F00", practice: "#D55E00" };

function Trend({ values, color, label }: { values: number[]; color: string; label: string }) {
  if (values.length < 2) return null;
  const max = Math.max(...values), min = Math.min(...values), span = max - min || 1;
  const pts = values.map((v, i) => `${(i / (values.length - 1)) * 100},${36 - ((v - min) / span) * 32}`).join(" ");
  return <svg viewBox="0 0 100 40" className="h-14 w-full" role="img" aria-label={`${label}: ${values.join(", ")}`} preserveAspectRatio="none"><polyline points={pts} fill="none" stroke={color} strokeWidth={2} vectorEffect="non-scaling-stroke" /></svg>;
}

export default function History() {
  const { t, history, setHistory, showResult } = useApp();
  const [msg, setMsg] = useState("");
  const stamp = new Date().toISOString().slice(0, 10);
  return (
    <Page title={t("nav_history")} actions={<>
      <button className="btn" disabled={!history.length} onClick={async () => (await saveFile(`color-vision-history-${stamp}.csv`, historyToCsv(history), "text/csv")) && setMsg(t("saved"))}><Icon name="download" size={18} />{t("export_csv")}</button>
      <button className="btn" disabled={!history.length} onClick={async () => (await saveFile(`color-vision-backup-${stamp}.json`, historyToJson(history), "application/json")) && setMsg(t("saved"))}>{t("export_json")}</button>
      <button className="btn" onClick={async () => { const txt = await pickTextFile("application/json,.json"); if (txt) { const ids = new Set(history.map((h) => h.id)); setHistory([...history, ...parseHistoryJson(txt).filter((e) => !ids.has(e.id))].sort((a, b) => b.timestamp - a.timestamp)); setMsg(t("saved")); } }}>{t("import_json")}</button>
    </>}>
      <p aria-live="polite" className="text-ok text-sm">{msg}</p>
      {!history.length ? <div className="card muted p-8 text-center">{t("hist_empty")}</div> : (
        <>
          <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
            {KINDS.map((k) => { const v = history.filter((h) => h.kind === k).map((h) => h.score).reverse(); return v.length ? (
              <div key={k} className="card p-4"><div className="flex justify-between text-sm"><span className="font-semibold">{t(TITLE[k])}</span><span className="muted">{v.length}×</span></div><Trend values={v} color={COLORS[k]} label={t(TITLE[k])} /></div>
            ) : null; })}
          </div>
          <ul className="card mt-6 divide-y divide-[var(--line)]">
            {history.map((h) => (
              <li key={h.id}><button className="flex w-full items-center justify-between gap-3 p-4 text-left hover:bg-black/5" onClick={() => showResult(h)}>
                <span><span className="mr-2 inline-block h-2.5 w-2.5 rounded-full" style={{ background: COLORS[h.kind] }} />{t(TITLE[h.kind])}{h.classification && <span className="muted"> · {t(`cls_${h.classification}`)}</span>}</span>
                <span className="muted text-sm">{h.score} · {new Date(h.timestamp).toLocaleDateString()}</span>
              </button></li>
            ))}
          </ul>
          <button className="btn mt-6 text-bad" onClick={async () => (await askConfirm(t("hist_confirm"))) && setHistory([])}>{t("hist_clear")}</button>
        </>
      )}
    </Page>
  );
}
