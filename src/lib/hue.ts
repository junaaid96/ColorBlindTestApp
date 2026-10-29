import { CVD_TYPES, labToRgb, clampRgb, simulate, toLab, type CvdType, type Rgb } from "./color";

/** D-15 style arrangement scored with Vingrys & King-Smith (1988) moments in CIE Lab. */
export const CAPS = 15;
export interface ArrangementResult { angle: number; cIndex: number; sIndex: number; crossings: number; likelyAxis: CvdType | null; passed: boolean }

const deg = (r: number) => (r * 180) / Math.PI;
/** Pilot cap (index 0) followed by CAPS movable caps in the correct order. */
export function caps(lightness = 58, chroma = 28): Rgb[] {
  return Array.from({ length: CAPS + 1 }, (_, i) => {
    const h = ((250 - i * 20) * Math.PI) / 180;
    return clampRgb(labToRgb({ l: lightness, a: chroma * Math.cos(h), b: chroma * Math.sin(h) }));
  });
}
const ab = (c: Rgb): [number, number] => { const l = toLab(c); return [l.a, l.b]; };

function majorAngle(sxx: number, syy: number, sxy: number) {
  let d = deg(0.5 * Math.atan2(2 * sxy, sxx - syy));
  if (d > 90) d -= 180;
  if (d < -90) d += 180;
  return d;
}

export function axisAngle(type: CvdType) {
  let sxx = 0, syy = 0, sxy = 0;
  for (const c of caps()) {
    const [a1, b1] = ab(c), [a2, b2] = ab(simulate(c, type));
    const dx = a1 - a2, dy = b1 - b2;
    sxx += dx * dx; syy += dy * dy; sxy += dx * dy;
  }
  return majorAngle(sxx, syy, sxy);
}

function moments(order: number[]): [number, number, number] {
  const pts = caps().map(ab);
  const seq = [0, ...order];
  let sxx = 0, syy = 0, sxy = 0;
  for (let i = 1; i < seq.length; i++) {
    const [x1, y1] = pts[seq[i - 1]], [x2, y2] = pts[seq[i]];
    const dx = x2 - x1, dy = y2 - y1;
    sxx += dx * dx; syy += dy * dy; sxy += dx * dy;
  }
  return [sxx, syy, sxy];
}

function radii([sxx, syy, sxy]: [number, number, number]): [number, number] {
  const tr = sxx + syy;
  const det = Math.sqrt(((sxx - syy) / 2) ** 2 + sxy * sxy);
  return [Math.sqrt((tr / 2 + det) / CAPS), Math.sqrt(Math.max(1e-9, tr / 2 - det) / CAPS)];
}

/** `order` lists cap indexes 1..CAPS as the person placed them. */
export function scoreArrangement(order: number[]): ArrangementResult {
  const sorted = [...order].sort((a, b) => a - b);
  if (sorted.length !== CAPS || sorted.some((v, i) => v !== i + 1)) throw new Error(`order must be a permutation of 1..${CAPS}`);
  const m = moments(order);
  const [major, minor] = radii(m);
  const [refMajor] = radii(moments(Array.from({ length: CAPS }, (_, i) => i + 1)));
  const angle = majorAngle(...m);
  let crossings = Math.abs(order[0]) > 2 ? 1 : 0;
  for (let i = 1; i < order.length; i++) if (Math.abs(order[i] - order[i - 1]) > 2) crossings++;
  const cIndex = major / refMajor;
  const passed = cIndex < 1.6 && crossings <= 1;
  let likelyAxis: CvdType | null = null;
  if (!passed) {
    let best = Infinity;
    for (const t of CVD_TYPES) {
      const d = Math.abs(angle - axisAngle(t)) % 180;
      const dist = Math.min(d, 180 - d);
      if (dist < best) { best = dist; likelyAxis = t; }
    }
  }
  return { angle, cIndex, sIndex: major / minor, crossings, likelyAxis, passed };
}
