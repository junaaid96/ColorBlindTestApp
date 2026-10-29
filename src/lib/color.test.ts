import { describe, expect, it } from "vitest";
import { contrastRatio, correctionMatrix, daltonize, deltaE, fromHex, hex, hsl, labToRgb, rgb, simulate, simulationMatrix, toGamma, toLab, toLinear, applyMatrixToPixels, MACHADO } from "./color";
import { nameColor } from "./namer";

describe("colour math", () => {
  it("round-trips gamma", () => { for (const v of [0, 0.02, 0.3, 0.75, 1]) expect(toGamma(toLinear(v))).toBeCloseTo(v, 9); });
  it("hex round-trips", () => { expect(hex(fromHex("#1E63E9"))).toBe("#1E63E9"); });
  it("WCAG contrast black/white is 21", () => { expect(contrastRatio(rgb(0, 0, 0), rgb(1, 1, 1))).toBeCloseTo(21, 5); });
  it("severity 0 is identity", () => { expect(simulationMatrix("protan", 0)).toEqual([1, 0, 0, 0, 1, 0, 0, 0, 1]); });
  it("severity 1 is Machado", () => { expect(simulationMatrix("deutan", 1)).toEqual([...MACHADO.deutan]); });
  it("Machado rows sum to ~1 so greys stay grey", () => {
    for (const m of Object.values(MACHADO)) for (let r = 0; r < 3; r++) expect(m[r * 3] + m[r * 3 + 1] + m[r * 3 + 2]).toBeCloseTo(1, 2);
    const g = simulate(rgb(0.5, 0.5, 0.5), "tritan");
    expect(deltaE(g, rgb(0.5, 0.5, 0.5))).toBeLessThan(1);
  });
  it("red and green collapse for deutans but not normally", () => {
    const red = hsl(0, 0.6, 0.45), green = hsl(80, 0.6, 0.38);
    expect(deltaE(simulate(red, "deutan"), simulate(green, "deutan"))).toBeLessThan(deltaE(red, green) / 2);
  });
  it("Lab round trip", () => { const c = rgb(0.2, 0.6, 0.4); expect(deltaE(labToRgb(toLab(c)), c)).toBeLessThan(0.01); });
  it("white is L=100", () => { expect(toLab(rgb(1, 1, 1)).l).toBeCloseTo(100, 0); });
  it("daltonize keeps greys", () => { expect(deltaE(daltonize(rgb(0.4, 0.4, 0.4), "protan"), rgb(0.4, 0.4, 0.4))).toBeLessThan(1.5); });
  it("correction matrix at severity 0 is identity", () => { correctionMatrix("tritan", 0).forEach((v, i) => expect(v).toBeCloseTo([1, 0, 0, 0, 1, 0, 0, 0, 1][i], 9)); });
  it("pixel filter matches per-colour simulate", () => {
    const px = new Uint8ClampedArray([200, 40, 60, 255]);
    applyMatrixToPixels(px, simulationMatrix("protan", 1));
    expect(hex(rgb(px[0] / 255, px[1] / 255, px[2] / 255))).toBe(hex(simulate(rgb(200 / 255, 40 / 255, 60 / 255), "protan")));
    expect(px[3]).toBe(255);
  });
  it("names colours", () => { expect(nameColor(fromHex(0x2e7d32)).name).toBe("Green"); expect(nameColor(rgb(0.99, 0.99, 0.99)).confidence).toBeGreaterThan(0.9); });
});
