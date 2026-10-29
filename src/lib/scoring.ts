import type { Plate, PlateKind } from "./plates";

/** Overall screening outcome. Never a diagnosis. */
export type Classification = "typical" | "protan" | "deutan" | "redGreen" | "tritan" | "inconclusive";
export type Severity = "none" | "mild" | "moderate" | "strong";
export interface PlateOutcome { plate: number; kind: PlateKind; expected: string; given: string; correct: boolean }
export interface ScreeningResult {
  classification: Classification;
  severity: Severity;
  /** 0..1 */
  confidence: number;
  /** 0..100 */
  score: number;
  outcomes: PlateOutcome[];
}

export const severityFor = (e: number): Severity => (e < 0.25 ? "none" : e < 0.5 ? "mild" : e < 0.8 ? "moderate" : "strong");

/** `answers[i]` is what the person typed for plate i ("" = saw nothing). */
export function scorePlates(plates: Plate[], answers: string[]): ScreeningResult {
  if (plates.length !== answers.length) throw new Error("one answer per plate");
  const outcomes = plates.map((p, i) => {
    const given = answers[i].trim();
    return { plate: p.id, kind: p.kind, expected: p.answer, given, correct: given === p.answer };
  });
  const rate = (k: PlateKind) => {
    const l = outcomes.filter((o) => o.kind === k);
    return l.length ? l.filter((o) => !o.correct).length / l.length : 0;
  };
  const controlFailed = outcomes.some((o) => o.kind === "control" && !o.correct);
  const rg = rate("redGreen"), tr = rate("tritan");
  // Diagnostic plates: protans miss the left digit, deutans the right one.
  let protan = 0, deutan = 0;
  for (const o of outcomes.filter((o) => o.kind === "diagnostic" && !o.correct)) {
    if (o.given === o.expected.slice(1)) protan++;
    else if (o.given === o.expected.slice(0, 1)) deutan++;
  }
  const classification: Classification = controlFailed ? "inconclusive"
    : rg >= 0.25 && rg >= tr ? (protan > deutan ? "protan" : deutan > protan ? "deutan" : "redGreen")
    : tr >= 0.34 ? "tritan" : "typical";
  const severity: Severity = classification === "typical" || classification === "inconclusive" ? "none"
    : classification === "tritan" ? severityFor(tr) : severityFor(rg);
  const sizeFactor = Math.min(1, outcomes.length / 15);
  const consistency = {
    typical: 1 - Math.max(rg, tr), inconclusive: 0.2, protan: 0.6 + 0.4 * rg, deutan: 0.6 + 0.4 * rg,
    redGreen: 0.4 + 0.4 * rg, tritan: 0.5 + 0.4 * tr,
  }[classification];
  const confidence = Math.min(1, Math.max(0, (0.35 + 0.65 * sizeFactor) * consistency));
  const score = outcomes.length ? Math.floor((outcomes.filter((o) => o.correct).length * 100) / outcomes.length) : 0;
  return { classification, severity, confidence, score, outcomes };
}
