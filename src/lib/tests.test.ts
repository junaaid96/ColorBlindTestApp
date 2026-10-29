import { describe, expect, it } from "vitest";
import { hit } from "./digitFont";
import { planPlates, generatePlate, seenDistance, packDots } from "./plates";
import { scorePlates, severityFor } from "./scoring";
import { CAPS, scoreArrangement } from "./hue";
import { ContrastStaircase } from "./staircase";
import { oddHueRound } from "./oddHue";
import { recordStreak, activeStreak, EMPTY_STREAK } from "./streak";
import { fromHex } from "./color";
import { historyToCsv, parseHistoryJson, historyToJson } from "./report";

describe("plates", () => {
  it("digit font hits figure centre", () => { expect(hit("8", 0.5, 0.5)).toBe(0); expect(hit("8", 0.05, 0.05)).toBe(-1); });
  it("plans 7 quick and 16 full plates", () => { expect(planPlates(false)).toHaveLength(7); expect(planPlates(true)).toHaveLength(16); });
  it("is deterministic per seed", () => { expect(generatePlate(3, "redGreen", "5", 1).dots).toEqual(generatePlate(3, "redGreen", "5", 1).dots); });
  it("packs many non-overlapping dots", () => { expect(packDots(42).length).toBeGreaterThan(400); });
  it("red-green figure hides for deutans", () => {
    const p = generatePlate(2, "redGreen", "7", 0);
    const fig = p.dots.find((d) => hit("7", d.x, d.y) >= 0)!; const bg = p.dots.find((d) => hit("7", d.x, d.y) < 0)!;
    expect(seenDistance(fromHex(fig.color), fromHex(bg.color), "deutan")).toBeLessThan(seenDistance(fromHex(fig.color), fromHex(bg.color), null));
  });
});

describe("scoring", () => {
  const plates = planPlates(true, 7);
  it("all correct is typical", () => { const r = scorePlates(plates, plates.map((p) => p.answer)); expect(r.classification).toBe("typical"); expect(r.score).toBe(100); });
  it("failed control is inconclusive", () => { const a = plates.map((p) => p.answer); a[0] = "x"; expect(scorePlates(plates, a).classification).toBe("inconclusive"); });
  it("missing red-green + left diag digit is protan", () => {
    const a = plates.map((p) => (p.kind === "redGreen" ? "" : p.kind === "diagnostic" ? p.answer.slice(1) : p.answer));
    const r = scorePlates(plates, a); expect(r.classification).toBe("protan"); expect(r.severity).toBe("strong");
  });
  it("deutan pattern", () => {
    const a = plates.map((p) => (p.kind === "redGreen" ? "" : p.kind === "diagnostic" ? p.answer.slice(0, 1) : p.answer));
    expect(scorePlates(plates, a).classification).toBe("deutan");
  });
  it("tritan pattern", () => { expect(scorePlates(plates, plates.map((p) => (p.kind === "tritan" ? "" : p.answer))).classification).toBe("tritan"); });
  it("severity bands", () => { expect([0.1, 0.3, 0.6, 0.9].map(severityFor)).toEqual(["none", "mild", "moderate", "strong"]); });
  it("rejects mismatched answers", () => { expect(() => scorePlates(plates, [])).toThrow(); });
});

describe("hue arrangement", () => {
  const perfect = Array.from({ length: CAPS }, (_, i) => i + 1);
  it("perfect order passes with C-index 1", () => { const r = scoreArrangement(perfect); expect(r.passed).toBe(true); expect(r.cIndex).toBeCloseTo(1, 6); expect(r.crossings).toBe(0); });
  it("scrambled order fails with an axis", () => {
    const r = scoreArrangement([1, 15, 2, 14, 3, 13, 4, 12, 5, 11, 6, 10, 7, 9, 8]);
    expect(r.passed).toBe(false); expect(r.likelyAxis).not.toBeNull(); expect(r.cIndex).toBeGreaterThan(1.6);
  });
  it("rejects non-permutations", () => { expect(() => scoreArrangement([1, 1, 2])).toThrow(); });
});

describe("staircase, practice, streak", () => {
  it("staircase converges and finishes", () => {
    const s = new ContrastStaircase();
    while (!s.finished) s.answer(s.contrast > 0.02);
    expect(s.threshold()).toBeGreaterThan(0.01); expect(s.threshold()).toBeLessThan(0.04);
    expect(s.logSensitivity()).toBeGreaterThan(1.3);
  });
  it("odd hue gets harder", () => {
    const r0 = oddHueRound(0, () => 0.5), r20 = oddHueRound(20, () => 0.5);
    expect(r20.size).toBeGreaterThan(r0.size); expect(r20.oddIndex).toBeLessThan(r20.size ** 2);
  });
  it("streak counts consecutive days", () => {
    let s = recordStreak(EMPTY_STREAK, 100); s = recordStreak(s, 101); s = recordStreak(s, 101);
    expect(s.current).toBe(2); expect(activeStreak(s, 103)).toBe(0);
    s = recordStreak(s, 105); expect(s).toEqual({ current: 1, best: 2, lastDay: 105 });
  });
});

describe("report", () => {
  const e = { id: "1", timestamp: 0, kind: "quick" as const, classification: "typical" as const, severity: "none" as const, score: 100, confidence: 90 };
  it("csv", () => { expect(historyToCsv([e]).split("\n")[1]).toBe("1970-01-01T00:00:00.000Z,quick,typical,none,100,90"); });
  it("json round trip", () => { expect(parseHistoryJson(historyToJson([e]))).toEqual([e]); });
  it("drops malformed", () => { expect(parseHistoryJson('[{"x":1}]')).toEqual([]); });
});
