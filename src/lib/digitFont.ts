/** Tiny 5x7 bitmap font used to rasterise plate figures without any copyrighted artwork. */
const GLYPHS: Record<string, string[]> = {
  "0": ["01110", "10001", "10011", "10101", "11001", "10001", "01110"],
  "1": ["00100", "01100", "00100", "00100", "00100", "00100", "01110"],
  "2": ["01110", "10001", "00001", "00010", "00100", "01000", "11111"],
  "3": ["11110", "00001", "00001", "01110", "00001", "00001", "11110"],
  "4": ["00010", "00110", "01010", "10010", "11111", "00010", "00010"],
  "5": ["11111", "10000", "11110", "00001", "00001", "10001", "01110"],
  "6": ["00110", "01000", "10000", "11110", "10001", "10001", "01110"],
  "7": ["11111", "00001", "00010", "00100", "01000", "01000", "01000"],
  "8": ["01110", "10001", "10001", "01110", "10001", "10001", "01110"],
  "9": ["01110", "10001", "10001", "01111", "00001", "00010", "01100"],
};

/** Index of the character of `text` covering (x, y) in the unit square, or -1 for background. */
export function hit(text: string, x: number, y: number): number {
  const n = text.length;
  const glyphW = (0.62 / n) * (n === 1 ? 1 : 1.25);
  const glyphH = 0.62;
  const total = glyphW * n + 0.06 * (n - 1);
  const left = 0.5 - total / 2, top = 0.5 - glyphH / 2;
  if (y < top || y >= top + glyphH) return -1;
  for (let i = 0; i < n; i++) {
    const gx = left + i * (glyphW + 0.06);
    if (x >= gx && x < gx + glyphW) {
      const rows = GLYPHS[text[i]];
      if (!rows) return -1;
      const col = Math.min(4, Math.max(0, Math.floor(((x - gx) / glyphW) * 5)));
      const row = Math.min(6, Math.max(0, Math.floor(((y - top) / glyphH) * 7)));
      return rows[row][col] === "1" ? i : -1;
    }
  }
  return -1;
}
