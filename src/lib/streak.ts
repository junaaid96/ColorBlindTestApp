/** Streak bookkeeping (epoch days), pure so it is unit tested. */
export interface Streak { current: number; best: number; lastDay: number }
export const EMPTY_STREAK: Streak = { current: 0, best: 0, lastDay: -1 };
export const epochDay = (t = Date.now()) => Math.floor((t - new Date(t).getTimezoneOffset() * 60000) / 86400000);
export function recordStreak(s: Streak, today: number): Streak {
  if (s.lastDay === today) return s;
  if (s.lastDay === today - 1) { const c = s.current + 1; return { current: c, best: Math.max(s.best, c), lastDay: today }; }
  return { current: 1, best: Math.max(s.best, 1), lastDay: today };
}
export const activeStreak = (s: Streak, today: number) => (s.lastDay >= today - 1 ? s.current : 0);
