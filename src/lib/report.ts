import type { HistoryEntry } from "./models";

const CSV_HEAD = ["timestamp", "kind", "classification", "severity", "score", "confidence"];
const esc = (v: unknown) => { const s = String(v ?? ""); return /[",\n]/.test(s) ? `"${s.replace(/"/g, '""')}"` : s; };

export function historyToCsv(list: HistoryEntry[]): string {
  return [CSV_HEAD.join(","), ...list.map((e) => [new Date(e.timestamp).toISOString(), e.kind, e.classification, e.severity, e.score, e.confidence].map(esc).join(","))].join("\n") + "\n";
}

export function historyToJson(list: HistoryEntry[]): string {
  return JSON.stringify({ app: "Color Vision Lab", version: 3, exported: new Date().toISOString(), entries: list }, null, 2);
}

/** Validates an imported JSON backup; returns only well-formed entries. */
export function parseHistoryJson(text: string): HistoryEntry[] {
  const data = JSON.parse(text);
  const list: unknown[] = Array.isArray(data) ? data : data?.entries ?? [];
  return list.filter((e): e is HistoryEntry => {
    const x = e as HistoryEntry;
    return !!x && typeof x.timestamp === "number" && typeof x.kind === "string" && typeof x.score === "number";
  }).map((e) => ({ ...e, id: e.id ?? `${e.timestamp}-${e.kind}` }));
}

/** Self-contained printable HTML report (print to PDF from any browser or the OS dialog). */
export function resultReportHtml(e: HistoryEntry, labels: { title: string; disclaimer: string; type: string; severity: string; confidence: string; score: string }): string {
  const rows = Array.isArray(e.details)
    ? e.details.map((o) => `<tr><td>${o.plate}</td><td>${o.kind}</td><td>${esc(o.expected)}</td><td>${esc(o.given) || "—"}</td><td>${o.correct ? "✓" : "✗"}</td></tr>`).join("")
    : "";
  return `<!doctype html><html><head><meta charset="utf-8"><title>${labels.title}</title><style>body{font:14px system-ui;margin:2rem;max-width:42rem}td,th{border:1px solid #ccc;padding:4px 8px}table{border-collapse:collapse}</style></head><body>
<h1>${labels.title}</h1><p>${new Date(e.timestamp).toLocaleString()}</p>
<ul><li>${labels.type}: ${e.classification ?? "—"}</li><li>${labels.severity}: ${e.severity ?? "—"}</li><li>${labels.confidence}: ${e.confidence}%</li><li>${labels.score}: ${e.score}</li></ul>
${rows ? `<table><tr><th>#</th><th>Kind</th><th>Expected</th><th>Given</th><th></th></tr>${rows}</table>` : ""}
<p><small>${labels.disclaimer}</small></p></body></html>`;
}
