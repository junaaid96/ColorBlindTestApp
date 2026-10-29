import { lazy, Suspense, useEffect, useState } from "react";
import { NAV, useApp, type Route } from "./state";
import { Icon } from "./components/ui";
import Home from "./screens/Home";
import { onDeepLink } from "./platform";
import type { Key } from "./i18n";

const Tests = lazy(() => import("./screens/Tests"));
const PlateTest = lazy(() => import("./screens/PlateTest"));
const Result = lazy(() => import("./screens/Result"));
const Arrangement = lazy(() => import("./screens/Arrangement"));
const Contrast = lazy(() => import("./screens/Contrast"));
const Lens = lazy(() => import("./screens/Lens"));
const Simulator = lazy(() => import("./screens/Simulator"));
const History = lazy(() => import("./screens/History"));
const Practice = lazy(() => import("./screens/Practice"));
const Settings = lazy(() => import("./screens/Settings"));

const ALL: Route[] = ["home", "tests", "quick", "full", "arrangement", "contrast", "lens", "simulator", "history", "practice", "settings"];
const LABEL: Record<string, Key> = { home: "nav_home", tests: "nav_tests", lens: "nav_lens", simulator: "nav_simulator", history: "nav_history", practice: "nav_practice", settings: "nav_settings" };

function Screen({ route }: { route: Route }) {
  switch (route) {
    case "tests": return <Tests />;
    case "quick": return <PlateTest key="q" full={false} />;
    case "full": return <PlateTest key="f" full />;
    case "result": return <Result />;
    case "arrangement": return <Arrangement />;
    case "contrast": return <Contrast />;
    case "lens": return <Lens />;
    case "simulator": return <Simulator />;
    case "history": return <History />;
    case "practice": return <Practice />;
    case "settings": return <Settings />;
    default: return <Home />;
  }
}

export default function App() {
  const { route, go, t, ready } = useApp();
  const [help, setHelp] = useState(false);

  useEffect(() => {
    const k = (e: KeyboardEvent) => {
      const el = e.target as HTMLElement;
      if (e.metaKey || e.ctrlKey || e.altKey || ["INPUT", "TEXTAREA", "SELECT"].includes(el.tagName)) return;
      if (route === "quick" || route === "full") return; // digits answer plates there
      const n = Number(e.key);
      if (n >= 1 && n <= NAV.length) go(NAV[n - 1]);
      if (e.key === "?") setHelp((h) => !h);
      if (e.key === "Escape") setHelp(false);
    };
    addEventListener("keydown", k);
    return () => removeEventListener("keydown", k);
  }, [go, route]);

  useEffect(() => {
    let off: (() => void) | undefined;
    onDeepLink((r) => { if ((ALL as string[]).includes(r)) go(r as Route); }).then((f) => (off = f));
    return () => off?.();
  }, [go]);

  if (!ready) return null;
  return (
    <div className="flex min-h-dvh">
      <a href="#main" className="sr-only focus:not-sr-only focus:fixed focus:left-2 focus:top-2 focus:z-50 btn">Skip to content</a>
      <nav aria-label="Main" className="fixed inset-x-0 bottom-0 z-20 flex justify-around border-t border-[var(--line)] bg-[var(--card)] pb-[env(safe-area-inset-bottom)] md:static md:w-60 md:flex-col md:justify-start md:gap-1 md:border-r md:border-t-0 md:p-4">
        <div className="mb-6 hidden items-center gap-2 px-3 text-lg font-bold md:flex"><img src="./favicon.svg" alt="" width={28} height={28} />{t("appName")}</div>
        {NAV.map((r, i) => (
          <button key={r} onClick={() => go(r)} aria-current={route === r ? "page" : undefined} title={`${t(LABEL[r])} (${i + 1})`}
            className={`flex flex-col items-center gap-0.5 rounded-xl px-2 py-2 text-[11px] md:flex-row md:gap-3 md:px-3 md:text-sm ${route === r ? "bg-primary/15 text-primary font-semibold" : "muted hover:bg-black/5"} ${r === "practice" || r === "history" ? "max-sm:hidden" : ""}`}>
            <Icon name={r} size={20} /><span>{t(LABEL[r])}</span>
          </button>
        ))}
      </nav>
      <main id="main" tabIndex={-1} className="flex-1 overflow-x-hidden pb-24 md:pb-0">
        <Suspense fallback={<div className="p-8 muted">…</div>}><Screen route={route} /></Suspense>
      </main>
      {help && (
        <div role="dialog" aria-modal="true" aria-label="Shortcuts" className="fixed inset-0 z-50 grid place-items-center bg-black/50 p-4" onClick={() => setHelp(false)}>
          <div className="card max-w-sm p-6">
            <h2 className="mb-3 font-semibold">⌨️</h2>
            <ul className="space-y-1 text-sm">{NAV.map((r, i) => <li key={r}><kbd className="chip !py-0">{i + 1}</kbd> {t(LABEL[r])}</li>)}<li><kbd className="chip !py-0">?</kbd> {t("shortcut_help")}</li></ul>
          </div>
        </div>
      )}
    </div>
  );
}
