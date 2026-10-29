import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from "react";
import { DEFAULT_SETTINGS, type HistoryEntry, type Settings } from "./lib/models";
import { EMPTY_STREAK, epochDay, recordStreak, type Streak } from "./lib/streak";
import { translator, type T } from "./i18n";
import { haptic, kvGet, kvSet } from "./platform";

export type Route = "home" | "tests" | "quick" | "full" | "arrangement" | "contrast" | "lens" | "simulator" | "history" | "practice" | "settings" | "result";
export const NAV: Route[] = ["home", "tests", "lens", "simulator", "history", "practice", "settings"];

interface Ctx {
  ready: boolean;
  route: Route; go: (r: Route) => void;
  settings: Settings; update: (p: Partial<Settings>) => void;
  history: HistoryEntry[]; addResult: (e: Omit<HistoryEntry, "id" | "timestamp">) => HistoryEntry; setHistory: (h: HistoryEntry[]) => void;
  lastResult: HistoryEntry | null; showResult: (e: HistoryEntry) => void;
  streak: Streak; bestPractice: number; recordPractice: (score: number) => void;
  t: T; buzz: (k?: "success" | "error" | "tap") => void;
}
const AppCtx = createContext<Ctx | null>(null);
export const useApp = () => { const c = useContext(AppCtx); if (!c) throw new Error("no app"); return c; };

export function AppProvider({ children }: { children: ReactNode }) {
  const [ready, setReady] = useState(false);
  const [route, setRoute] = useState<Route>(() => (location.hash.slice(1) as Route) || "home");
  const [settings, setSettings] = useState<Settings>(DEFAULT_SETTINGS);
  const [history, setHistoryState] = useState<HistoryEntry[]>([]);
  const [streak, setStreak] = useState<Streak>(EMPTY_STREAK);
  const [bestPractice, setBest] = useState(0);
  const [lastResult, setLast] = useState<HistoryEntry | null>(null);

  useEffect(() => {
    (async () => {
      setSettings({ ...DEFAULT_SETTINGS, ...((await kvGet<Settings>("settings")) ?? {}) });
      setHistoryState((await kvGet<HistoryEntry[]>("history")) ?? []);
      setStreak((await kvGet<Streak>("streak")) ?? EMPTY_STREAK);
      setBest((await kvGet<number>("bestPractice")) ?? 0);
      setReady(true);
    })();
  }, []);

  useEffect(() => {
    const onHash = () => setRoute((location.hash.slice(1) as Route) || "home");
    addEventListener("hashchange", onHash);
    return () => removeEventListener("hashchange", onHash);
  }, []);

  // Theme, contrast and motion are applied as data attributes on <html>.
  useEffect(() => {
    const root = document.documentElement;
    const mq = matchMedia("(prefers-color-scheme: dark)");
    const apply = () => root.dataset.theme = settings.theme === "system" ? (mq.matches ? "dark" : "light") : settings.theme;
    apply(); mq.addEventListener("change", apply);
    root.dataset.contrast = settings.highContrast ? "high" : "normal";
    root.dataset.motion = settings.reduceMotion || matchMedia("(prefers-reduced-motion: reduce)").matches ? "reduce" : "full";
    root.lang = settings.locale;
    return () => mq.removeEventListener("change", apply);
  }, [settings]);

  const go = useCallback((r: Route) => { location.hash = r; setRoute(r); }, []);
  const update = useCallback((p: Partial<Settings>) => setSettings((s) => { const n = { ...s, ...p }; kvSet("settings", n); return n; }), []);
  const setHistory = useCallback((h: HistoryEntry[]) => { setHistoryState(h); kvSet("history", h); }, []);
  const addResult = useCallback((e: Omit<HistoryEntry, "id" | "timestamp">) => {
    const entry: HistoryEntry = { ...e, id: crypto.randomUUID(), timestamp: Date.now() };
    setHistoryState((h) => { const n = [entry, ...h].slice(0, 500); kvSet("history", n); return n; });
    return entry;
  }, []);
  const showResult = useCallback((e: HistoryEntry) => { setLast(e); go("result"); }, [go]);
  const recordPractice = useCallback((score: number) => {
    setStreak((s) => { const n = recordStreak(s, epochDay()); kvSet("streak", n); return n; });
    setBest((b) => { const n = Math.max(b, score); kvSet("bestPractice", n); return n; });
  }, []);
  const buzz = useCallback((k: "success" | "error" | "tap" = "tap") => { if (settings.haptics) haptic(k); }, [settings.haptics]);
  const t = useMemo(() => translator(settings.locale), [settings.locale]);

  const value = { ready, route, go, settings, update, history, addResult, setHistory, lastResult, showResult, streak, bestPractice, recordPractice, t, buzz };
  return <AppCtx.Provider value={value}>{children}</AppCtx.Provider>;
}
