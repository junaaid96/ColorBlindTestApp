import { useState } from "react";
import { useApp, type Route } from "../state";
import { Icon } from "../components/ui";
import { activeStreak, epochDay } from "../lib/streak";
import type { Key } from "../i18n";

const CARDS: { route: Route; title: Key; desc: Key; icon: string; accent: string }[] = [
  { route: "quick", title: "quick_title", desc: "quick_desc", icon: "tests", accent: "from-brand to-sky" },
  { route: "full", title: "full_title", desc: "full_desc", icon: "tests", accent: "from-ok to-sky" },
  { route: "arrangement", title: "arr_title", desc: "arr_desc", icon: "practice", accent: "from-pink to-warn" },
  { route: "contrast", title: "contrast_title", desc: "contrast_desc", icon: "arrow", accent: "from-[#444] to-[#999]" },
  { route: "lens", title: "lens_title", desc: "lens_desc", icon: "lens", accent: "from-warn to-bad" },
  { route: "simulator", title: "sim_title", desc: "sim_desc", icon: "simulator", accent: "from-sky to-pink" },
  { route: "practice", title: "practice_title", desc: "practice_desc", icon: "practice", accent: "from-sun to-ok" },
];

export function TestCards({ only }: { only?: Route[] }) {
  const { t, go } = useApp();
  return (
    <ul className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {CARDS.filter((c) => !only || only.includes(c.route)).map((c) => (
        <li key={c.route}>
          <button onClick={() => go(c.route)} className="card group flex h-full w-full items-start gap-4 p-5 text-left transition hover:-translate-y-0.5 hover:shadow-lg">
            <span className={`grid h-12 w-12 shrink-0 place-items-center rounded-2xl bg-gradient-to-br ${c.accent} text-white`}><Icon name={c.icon} /></span>
            <span><span className="block font-semibold">{t(c.title)}</span><span className="muted text-sm">{t(c.desc)}</span></span>
          </button>
        </li>
      ))}
    </ul>
  );
}

export function Calibration({ onDone }: { onDone?: () => void }) {
  const { t, update } = useApp();
  return (
    <div className="card p-6">
      <h2 className="text-lg font-semibold">{t("cal_title")}</h2>
      <ol className="mt-3 list-decimal space-y-2 pl-5">{(["cal_1", "cal_2", "cal_3", "cal_4"] as Key[]).map((k) => <li key={k}>{t(k)}</li>)}</ol>
      <p className="muted mt-4 text-sm">{t("cal_grey")}</p>
      <div className="mt-2 flex h-10 overflow-hidden rounded-xl" aria-hidden>{Array.from({ length: 10 }, (_, i) => <div key={i} className="flex-1" style={{ background: `hsl(0 0% ${5 + i * 10}%)` }} />)}</div>
      {onDone && <button className="btn btn-primary mt-5" onClick={() => { update({ calibrated: true }); onDone(); }}>{t("continue")}</button>}
    </div>
  );
}

function Onboarding() {
  const { t, update } = useApp();
  const [i, setI] = useState(0);
  const steps: [Key, Key][] = [["onb_1_t", "onb_1_b"], ["onb_2_t", "onb_2_b"], ["onb_3_t", "onb_3_b"]];
  const last = i === steps.length;
  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-black/60 p-4 backdrop-blur" role="dialog" aria-modal="true" aria-label={t("appName")}>
      <div className="card w-full max-w-lg p-6">
        {last ? <Calibration /> : (
          <div className="fade-in" key={i}>
            <div className="mb-4 h-24 rounded-2xl" style={{ background: "conic-gradient(from 200deg, #0072B2, #56B4E9, #009E73, #F0E442, #E69F00, #D55E00, #CC79A7, #0072B2)" }} aria-hidden />
            <h2 className="text-xl font-bold">{t(steps[i][0])}</h2>
            <p className="muted mt-2">{t(steps[i][1])}</p>
          </div>
        )}
        <div className="mt-6 flex items-center justify-between">
          <div className="flex gap-1.5" aria-hidden>{[0, 1, 2, 3].map((d) => <span key={d} className={`h-2 w-6 rounded-full ${d === i ? "bg-brand" : "bg-[var(--line)]"}`} />)}</div>
          <div className="flex gap-2">
            {!last && <button className="btn" onClick={() => update({ onboarded: true })}>{t("skip")}</button>}
            <button className="btn btn-primary" onClick={() => (last ? update({ onboarded: true, calibrated: true }) : setI(i + 1))}>{last ? t("start") : t("next")}</button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function Home() {
  const { t, settings, history, streak, ready } = useApp();
  const last = history[0];
  return (
    <section className="fade-in mx-auto w-full max-w-5xl px-4 pb-28 pt-6 md:px-8">
      {ready && !settings.onboarded && <Onboarding />}
      <div className="relative overflow-hidden rounded-[2rem] p-8 text-white md:p-10" style={{ background: "linear-gradient(135deg,#0072B2,#0b3a5c 55%,#CC79A7)" }}>
        <p className="text-sm/6 opacity-80">v3 · Tauri</p>
        <h1 className="mt-1 text-3xl font-extrabold tracking-tight md:text-5xl">{t("appName")}</h1>
        <p className="mt-3 max-w-xl text-white/85">{t("tagline")}</p>
        <div className="mt-6 flex flex-wrap gap-3 text-sm">
          <span className="rounded-full bg-white/15 px-3 py-1">🔥 {activeStreak(streak, epochDay())} {t("practice_streak")}</span>
          {last && <span className="rounded-full bg-white/15 px-3 py-1">{t("result_score")}: {last.score}</span>}
        </div>
      </div>
      <h2 className="mb-3 mt-8 text-lg font-semibold">{t("nav_tests")}</h2>
      <TestCards />
      <p className="muted mt-8 text-sm" role="note">⚠️ {t("disclaimer")}</p>
    </section>
  );
}
