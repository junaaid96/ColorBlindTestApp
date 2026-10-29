import { useApp } from "../state";
import { Icon, Page, Stat } from "../components/ui";
import type { HistoryEntry } from "../lib/models";
import { resultReportHtml } from "../lib/report";
import { saveFile, share } from "../platform";
import { useEffect, useState } from "react";

export function summary(e: HistoryEntry, t: ReturnType<typeof useApp>["t"]) {
  const cls = e.classification ? t(`cls_${e.classification}`) : e.kind;
  return `${t("appName")} — ${new Date(e.timestamp).toLocaleString()}\n${cls}${e.severity && e.severity !== "none" ? ` (${t(`sev_${e.severity}`)})` : ""}\n${t("result_score")}: ${e.score} · ${t("result_confidence")}: ${e.confidence}%\n${t("disclaimer")}`;
}

export default function Result() {
  const { t, lastResult: e, go } = useApp();
  const [msg, setMsg] = useState("");
  // Redirect after render (e.g. a reload on #result) instead of updating state during render.
  useEffect(() => { if (!e) go("home"); }, [e, go]);
  if (!e) return null;
  const tone = e.classification === "typical" ? "ok" : e.classification === "inconclusive" ? "warn" : "bad";
  const exportReport = async () => {
    const html = resultReportHtml(e, { title: t("result_title"), disclaimer: t("disclaimer"), type: t("result_type"), severity: t("result_severity"), confidence: t("result_confidence"), score: t("result_score") });
    if (await saveFile(`color-vision-report-${new Date(e.timestamp).toISOString().slice(0, 10)}.html`, html, "text/html")) setMsg(t("saved"));
  };
  return (
    <Page title={t("result_title")} actions={<>
      <button className="btn" onClick={async () => setMsg((await share(t("appName"), summary(e, t))) === "copied" ? t("copied") : "")}><Icon name="share" size={18} />{t("share")}</button>
      <button className="btn" onClick={exportReport}><Icon name="download" size={18} />{t("export_report")}</button>
      <button className="btn btn-primary" onClick={() => go("tests")}>{t("retry")}</button>
    </>}>
      <p aria-live="polite" className="text-ok text-sm">{msg}</p>
      {e.classification && (
        <div className="card mb-6 p-6">
          <h2 className={`text-2xl font-bold ${{ ok: "text-ok", warn: "text-warn", bad: "text-bad" }[tone]}`}>{t(`cls_${e.classification}`)}</h2>
          <p className="muted mt-2">{t(`cls_${e.classification}_desc`)}</p>
        </div>
      )}
      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        <Stat label={t("result_type")} value={e.classification ? t(`cls_${e.classification}`).split(" (")[0] : "—"} tone={tone} />
        <Stat label={t("result_severity")} value={e.severity ? t(`sev_${e.severity}`) : "—"} />
        <Stat label={t("result_confidence")} value={`${e.confidence}%`} />
        <Stat label={t("result_score")} value={e.score} />
      </div>
      {Array.isArray(e.details) && (
        <div className="card mt-6 overflow-x-auto p-4">
          <h3 className="mb-3 font-semibold">{t("result_breakdown")}</h3>
          <table className="w-full text-sm">
            <thead className="muted text-left"><tr><th className="p-2">#</th><th className="p-2">Kind</th><th className="p-2">Expected</th><th className="p-2">Given</th><th className="p-2"><span className="sr-only">Correct</span></th></tr></thead>
            <tbody>{e.details.map((o) => (
              <tr key={o.plate} className="border-t border-[var(--line)]"><td className="p-2">{o.plate}</td><td className="p-2">{o.kind}</td><td className="p-2 font-mono">{o.expected}</td><td className="p-2 font-mono">{o.given || "—"}</td>
                <td className={`p-2 ${o.correct ? "text-ok" : "text-bad"}`}><Icon name={o.correct ? "check" : "x"} size={18} label={o.correct ? "correct" : "wrong"} /></td></tr>
            ))}</tbody>
          </table>
        </div>
      )}
      <p className="muted mt-6 text-sm">⚠️ {t("disclaimer")}</p>
    </Page>
  );
}
