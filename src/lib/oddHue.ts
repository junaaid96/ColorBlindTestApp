import { hsl, type Rgb } from "./color";

/** Daily practice: find the tile whose hue differs. Harder each level. */
export interface OddHueRound { size: number; base: Rgb; odd: Rgb; oddIndex: number }
export function oddHueRound(level: number, rand: () => number = Math.random): OddHueRound {
  const size = Math.min(7, 2 + Math.floor(level / 3));
  const hue = rand() * 360;
  const delta = Math.max(2.5, 40 * 0.82 ** level);
  const sat = 0.45 + rand() * 0.3, light = 0.4 + rand() * 0.2;
  return { size, base: hsl(hue, sat, light), odd: hsl(hue + delta, sat, light), oddIndex: Math.floor(rand() * size * size) };
}
