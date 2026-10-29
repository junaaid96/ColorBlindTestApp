import { deltaE, fromHex, type Rgb } from "./color";

/** Curated everyday palette; nearest entry by CIE Lab distance. */
const PALETTE: [string, number][] = [
  ["Black", 0x000000], ["Charcoal", 0x36454f], ["Gray", 0x808080], ["Silver", 0xc0c0c0],
  ["White", 0xffffff], ["Ivory", 0xfffff0], ["Beige", 0xf5f5dc], ["Cream", 0xfffdd0],
  ["Red", 0xe53935], ["Dark red", 0x8b0000], ["Maroon", 0x800000], ["Crimson", 0xdc143c],
  ["Pink", 0xffc0cb], ["Hot pink", 0xff69b4], ["Magenta", 0xff00ff], ["Rose", 0xff007f],
  ["Orange", 0xff8c00], ["Coral", 0xff7f50], ["Salmon", 0xfa8072], ["Peach", 0xffdab9],
  ["Brown", 0x8b4513], ["Chocolate", 0x5d3a1a], ["Tan", 0xd2b48c], ["Khaki", 0xc3b091],
  ["Gold", 0xffd700], ["Yellow", 0xffeb3b], ["Mustard", 0xe1ad01], ["Lemon", 0xfff44f],
  ["Olive", 0x808000], ["Lime", 0x9acd32], ["Green", 0x2e7d32], ["Bright green", 0x00ff00],
  ["Dark green", 0x013220], ["Mint", 0x98ff98], ["Sage", 0x9caf88], ["Forest green", 0x228b22],
  ["Teal", 0x008080], ["Turquoise", 0x40e0d0], ["Cyan", 0x00ffff], ["Aqua", 0x7fffd4],
  ["Sky blue", 0x87ceeb], ["Light blue", 0xadd8e6], ["Blue", 0x1e63e9], ["Navy", 0x000080],
  ["Royal blue", 0x4169e1], ["Indigo", 0x4b0082], ["Purple", 0x800080], ["Violet", 0x8f00ff],
  ["Lavender", 0xe6e6fa], ["Lilac", 0xc8a2c8], ["Plum", 0x8e4585],
];

export interface Named { name: string; rgb: Rgb; distance: number; confidence: number }

export function nameColor(c: Rgb): Named {
  let best: Named | null = null;
  for (const [name, h] of PALETTE) {
    const p = fromHex(h);
    const d = deltaE(c, p);
    if (!best || d < best.distance) best = { name, rgb: p, distance: d, confidence: 0 };
  }
  best!.confidence = Math.min(1, Math.max(0, 1 - best!.distance / 40));
  return best!;
}
