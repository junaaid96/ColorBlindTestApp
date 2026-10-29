import { clampRgb, deltaE, hex, hsl, luminance, rgb, simulate, type CvdType, type Rgb } from "./color";
import { hit } from "./digitFont";
import { Rng } from "./random";

/** What a plate is designed to reveal. */
export type PlateKind = "control" | "redGreen" | "tritan" | "diagnostic";
export interface Dot { x: number; y: number; r: number; color: string }
export interface Plate { id: number; kind: PlateKind; answer: string; difficulty: number; dots: Dot[] }

let candidateCache: Rgb[] | null = null;
function candidates(): Rgb[] {
  if (!candidateCache) {
    candidateCache = [];
    for (let h = 0; h < 360; h += 6) for (const s of [0.45, 0.6, 0.75]) for (const l of [0.42, 0.5, 0.58]) candidateCache.push(hsl(h, s, l));
  }
  return candidateCache;
}

/** Distance between two colours as seen by `type` (null = typical vision). */
export const seenDistance = (a: Rgb, b: Rgb, type: CvdType | null) =>
  type === null ? deltaE(a, b) : deltaE(simulate(a, type), simulate(b, type));

/** Figure colour hidden for every type in `hiddenFor`, visible for `visibleFor`. Difficulty 0..2. */
export function figureColor(bg: Rgb, hiddenFor: CvdType[], visibleFor: CvdType[], difficulty: number): Rgb {
  const targetNormal = difficulty === 0 ? 45 : difficulty === 1 ? 32 : 22;
  let best = candidates()[0], bestScore = Infinity;
  for (const c of candidates()) {
    const normal = seenDistance(c, bg, null);
    const hidden = hiddenFor.reduce((s, t) => s + seenDistance(c, bg, t), 0);
    const visible = visibleFor.length ? Math.min(...visibleFor.map((t) => seenDistance(c, bg, t))) : normal;
    const lum = Math.abs(luminance(c) - luminance(bg)) * 200;
    const score = hidden * 3 + lum + Math.abs(normal - targetNormal) - visible * 0.8;
    if (score < bestScore) { bestScore = score; best = c; }
  }
  return best;
}

/** Non-overlapping dots inside the unit circle, deterministic for `seed`. */
export function packDots(seed: number, count = 900): [number, number, number][] {
  const rnd = new Rng(seed);
  const placed: [number, number, number][] = [];
  let attempts = 0;
  while (placed.length < count && attempts < count * 30) {
    attempts++;
    const r = rnd.range(0.008, 0.024);
    const ang = rnd.range(0, Math.PI * 2);
    const rad = Math.sqrt(rnd.next()) * (0.48 - r);
    const x = 0.5 + rad * Math.cos(ang), y = 0.5 + rad * Math.sin(ang);
    if (!placed.some(([px, py, pr]) => (px - x) ** 2 + (py - y) ** 2 < (pr + r + 0.002) ** 2)) placed.push([x, y, r]);
  }
  return placed;
}

export function generatePlate(id: number, kind: PlateKind, answer: string, difficulty: number, seed = id * 7919): Plate {
  const rnd = new Rng(seed);
  let bg: Rgb, figures: Rgb[];
  switch (kind) {
    case "control": bg = rgb(0.55, 0.6, 0.62); figures = [rgb(0.92, 0.5, 0.15)]; break;
    case "redGreen": bg = hsl(95 + rnd.int(-10, 10), 0.45, 0.5); figures = [figureColor(bg, ["protan", "deutan"], [], difficulty)]; break;
    case "tritan": bg = hsl(60 + rnd.int(-8, 8), 0.5, 0.55); figures = [figureColor(bg, ["tritan"], ["protan", "deutan"], difficulty)]; break;
    case "diagnostic":
      bg = rgb(0.55, 0.55, 0.55);
      figures = [figureColor(bg, ["protan"], ["deutan"], 0), figureColor(bg, ["deutan"], ["protan"], 0)];
      break;
  }
  const dots = packDots(seed).map(([x, y, r]) => {
    const idx = hit(answer, x, y);
    const base = idx < 0 ? bg : figures[Math.min(idx, figures.length - 1)];
    const lift = rnd.range(-0.06, 0.06); // lightness noise defeats luminance cues
    const j = () => rnd.range(-0.035, 0.035);
    const c = clampRgb(rgb(base.r + lift + j(), base.g + lift + j(), base.b + lift + j()));
    return { x, y, r, color: hex(c) };
  });
  return { id, kind, answer, difficulty, dots };
}

/** Quick (7 plates, about a minute) or full (16 plates) screening plan. */
export function planPlates(full: boolean, seed = 1): Plate[] {
  const rnd = new Rng(seed);
  const num = (two: boolean) => (two ? `${rnd.int(1, 10)}${rnd.int(0, 10)}` : `${rnd.int(2, 10)}`);
  const spec: [PlateKind, number][] = [["control", 0]];
  const rg = full ? 9 : 4;
  for (let i = 0; i < rg; i++) spec.push(["redGreen", Math.floor((i * 3) / rg)]);
  for (let i = 0; i < (full ? 3 : 1); i++) spec.push(["tritan", i]);
  for (let i = 0; i < (full ? 2 : 1); i++) spec.push(["diagnostic", 0]);
  if (full) spec.push(["control", 0]);
  return spec.map(([kind, diff], i) => {
    const answer = kind === "diagnostic" ? `${rnd.int(1, 10)}${rnd.int(1, 10)}` : num(rnd.bool());
    return generatePlate(i + 1, kind, answer, diff, seed * 1000 + i);
  });
}
