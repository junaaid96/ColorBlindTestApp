/** 1-up/2-down adaptive staircase (~71% correct). Weber contrast in 0.002..1. */
export class ContrastStaircase {
  contrast: number;
  trials = 0;
  private streak = 0;
  private lastDirection = 0;
  private reversals: number[] = [];
  constructor(start = 0.4, private maxReversals = 6, private maxTrials = 30) { this.contrast = start; }
  get finished() { return this.reversals.length >= this.maxReversals || this.trials >= this.maxTrials; }
  get reversalCount() { return this.reversals.length; }
  answer(correct: boolean) {
    if (this.finished) return;
    this.trials++;
    let dir = 0;
    if (correct) { this.streak++; if (this.streak >= 2) { this.streak = 0; dir = -1; } } else { this.streak = 0; dir = 1; }
    if (dir !== 0) {
      if (this.lastDirection !== 0 && dir !== this.lastDirection) this.reversals.push(this.contrast);
      this.lastDirection = dir;
      this.contrast = Math.min(1, Math.max(0.002, dir < 0 ? this.contrast * 0.7 : this.contrast / 0.7));
    }
  }
  /** Geometric mean of the last four reversals (or the current level). */
  threshold() {
    const last = this.reversals.slice(-4);
    const l = last.length ? last : [this.contrast];
    return Math.exp(l.reduce((s, v) => s + Math.log(v), 0) / l.length);
  }
  /** Log contrast sensitivity; about 1.6-2.0 is typical on a screen. */
  logSensitivity() { return -Math.log10(this.threshold()); }
}
