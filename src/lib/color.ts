/**
 * Colour science, ported from the Kotlin app.
 * Simulation: Machado, Oliveira & Fernandes (2009) matrices in linear sRGB.
 * Daltonization: Fidaner, Lin & Ozguven (2005) error redistribution.
 */
export type CvdType = "protan" | "deutan" | "tritan";
export const CVD_TYPES: CvdType[] = ["protan", "deutan", "tritan"];

/** sRGB, gamma-encoded channels in 0..1. */
export interface Rgb { r: number; g: number; b: number }
export interface Lab { l: number; a: number; b: number }
export type Mat3 = readonly number[]; // row-major 3x3

const clamp01 = (v: number) => Math.min(1, Math.max(0, v));
export const rgb = (r: number, g: number, b: number): Rgb => ({ r, g, b });
export const clampRgb = (c: Rgb): Rgb => rgb(clamp01(c.r), clamp01(c.g), clamp01(c.b));

export function hex(c: Rgb): string {
  const k = clampRgb(c);
  return "#" + [k.r, k.g, k.b].map((v) => Math.round(v * 255).toString(16).padStart(2, "0")).join("").toUpperCase();
}
export function fromHex(h: string | number): Rgb {
  const n = typeof h === "number" ? h : parseInt(h.replace("#", ""), 16);
  return rgb(((n >> 16) & 255) / 255, ((n >> 8) & 255) / 255, (n & 255) / 255);
}
export function hsl(h: number, s: number, l: number): Rgb {
  const c = (1 - Math.abs(2 * l - 1)) * s;
  const hp = ((((h % 360) + 360) % 360) / 60);
  const x = c * (1 - Math.abs((hp % 2) - 1));
  const [r1, g1, b1] = hp < 1 ? [c, x, 0] : hp < 2 ? [x, c, 0] : hp < 3 ? [0, c, x] : hp < 4 ? [0, x, c] : hp < 5 ? [x, 0, c] : [c, 0, x];
  const m = l - c / 2;
  return rgb(r1 + m, g1 + m, b1 + m);
}

export const toLinear = (c: number) => (c <= 0.04045 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4);
export function toGamma(c: number) {
  const v = clamp01(c);
  return v <= 0.0031308 ? v * 12.92 : 1.055 * v ** (1 / 2.4) - 0.055;
}

/** WCAG relative luminance. */
export const luminance = (c: Rgb) => 0.2126 * toLinear(c.r) + 0.7152 * toLinear(c.g) + 0.0722 * toLinear(c.b);
export function contrastRatio(a: Rgb, b: Rgb) {
  const la = luminance(a), lb = luminance(b);
  return (Math.max(la, lb) + 0.05) / (Math.min(la, lb) + 0.05);
}

const IDENTITY: Mat3 = [1, 0, 0, 0, 1, 0, 0, 0, 1];
/** Machado 2009, severity 1.0 (dichromacy). Kept identical to src-tauri/src/color.rs. */
export const MACHADO: Record<CvdType, Mat3> = {
  protan: [0.152286, 1.052583, -0.204868, 0.114503, 0.786281, 0.099216, -0.003882, -0.048116, 1.051998],
  deutan: [0.367322, 0.860646, -0.227968, 0.280085, 0.672501, 0.047413, -0.01182, 0.04294, 0.968881],
  tritan: [1.255528, -0.076749, -0.178779, -0.078411, 0.930809, 0.147602, 0.004733, 0.691367, 0.3039],
};

/** Linear interpolation between identity and the dichromat matrix. */
export function simulationMatrix(type: CvdType, severity: number): number[] {
  const s = clamp01(severity);
  return IDENTITY.map((v, i) => v * (1 - s) + MACHADO[type][i] * s);
}

export function mul(m: Mat3, r: number, g: number, b: number): [number, number, number] {
  return [m[0] * r + m[1] * g + m[2] * b, m[3] * r + m[4] * g + m[5] * b, m[6] * r + m[7] * g + m[8] * b];
}

export function simulate(c: Rgb, type: CvdType, severity = 1): Rgb {
  const [r, g, b] = mul(simulationMatrix(type, severity), toLinear(c.r), toLinear(c.g), toLinear(c.b));
  return rgb(toGamma(r), toGamma(g), toGamma(b));
}

/** Correction matrix (linear RGB): I + K(I - M). Used for images and live camera. */
export function correctionMatrix(type: CvdType, severity: number): number[] {
  const m = simulationMatrix(type, severity);
  const err = IDENTITY.map((v, i) => v - m[i]);
  const k = type === "tritan" ? [0.7, 0, 0, 0, 0.7, 0, 0, 0, 0] : [0, 0, 0, 0.7, 0, 0, 0.7, 0, 1];
  return IDENTITY.map((v, i) => {
    const row = Math.floor(i / 3), col = i % 3;
    let s = 0;
    for (let j = 0; j < 3; j++) s += k[row * 3 + j] * err[j * 3 + col];
    return v + s;
  });
}

export function daltonize(c: Rgb, type: CvdType, severity = 1): Rgb {
  const r = toLinear(c.r), g = toLinear(c.g), b = toLinear(c.b);
  const [sr, sg, sb] = mul(simulationMatrix(type, severity), r, g, b);
  const er = r - sr, eg = g - sg, eb = b - sb;
  const out = type === "tritan" ? [er * 0.7 + r, eg * 0.7 + g, b] : [r, g + 0.7 * er, b + 0.7 * er + eb];
  return rgb(toGamma(out[0]), toGamma(out[1]), toGamma(out[2]));
}

export function toLab(c: Rgb): Lab {
  const r = toLinear(c.r), g = toLinear(c.g), b = toLinear(c.b);
  const x = (0.4124 * r + 0.3576 * g + 0.1805 * b) / 0.95047;
  const y = 0.2126 * r + 0.7152 * g + 0.0722 * b;
  const z = (0.0193 * r + 0.1192 * g + 0.9505 * b) / 1.08883;
  const f = (t: number) => (t > 0.008856 ? Math.cbrt(t) : 7.787 * t + 16 / 116);
  const fx = f(x), fy = f(y), fz = f(z);
  return { l: 116 * fy - 16, a: 500 * (fx - fy), b: 200 * (fy - fz) };
}

/** Inverse of toLab (D65). May be out of gamut; clamp afterwards. */
export function labToRgb({ l, a, b }: Lab): Rgb {
  const fy = (l + 16) / 116, fx = fy + a / 500, fz = fy - b / 200;
  const inv = (t: number) => (t ** 3 > 0.008856 ? t ** 3 : (t - 16 / 116) / 7.787);
  const x = inv(fx) * 0.95047, y = inv(fy), z = inv(fz) * 1.08883;
  return rgb(
    toGamma(3.2406 * x - 1.5372 * y - 0.4986 * z),
    toGamma(-0.9689 * x + 1.8758 * y + 0.0415 * z),
    toGamma(0.0557 * x - 0.204 * y + 1.057 * z),
  );
}

/** CIE76 distance; adequate for naming and plate design. */
export function deltaE(a: Rgb, b: Rgb) {
  const x = toLab(a), y = toLab(b);
  return Math.hypot(x.l - y.l, x.a - y.a, x.b - y.b);
}

/** Apply a linear-RGB 3x3 matrix to RGBA pixels in place (JS fallback for the Rust command). */
export function applyMatrixToPixels(data: Uint8ClampedArray | Uint8Array, m: Mat3) {
  const lin = new Float64Array(256);
  for (let i = 0; i < 256; i++) lin[i] = toLinear(i / 255);
  for (let i = 0; i < data.length; i += 4) {
    const [r, g, b] = mul(m, lin[data[i]], lin[data[i + 1]], lin[data[i + 2]]);
    data[i] = Math.round(toGamma(r) * 255);
    data[i + 1] = Math.round(toGamma(g) * 255);
    data[i + 2] = Math.round(toGamma(b) * 255);
  }
}

/**
 * SVG feColorMatrix values (applied in linearRGB, the SVG default) for live camera/video filters.
 */
export function svgFilterValues(m: Mat3): string {
  return [m[0], m[1], m[2], 0, 0, m[3], m[4], m[5], 0, 0, m[6], m[7], m[8], 0, 0, 0, 0, 0, 1, 0].map((v) => +v.toFixed(6)).join(" ");
}
