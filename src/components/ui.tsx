import { useEffect, useRef, type ReactNode } from "react";
import type { Plate } from "../lib/plates";
import { CVD_TYPES, correctionMatrix, simulationMatrix, svgFilterValues, type CvdType } from "../lib/color";
import { useApp } from "../state";

const PATHS: Record<string, string> = {
  home: "M3 11l9-7 9 7v9a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1z",
  tests: "M9 3h6v2l4 13a2 2 0 0 1-2 3H7a2 2 0 0 1-2-3L9 5z M8 14h8",
  lens: "M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10z M3 7V4h3 M21 7V4h-3 M3 17v3h3 M21 17v3h-3",
  simulator: "M4 5h16v14H4z M4 15l5-5 4 4 3-3 4 4",
  history: "M3 12a9 9 0 1 0 3-6.7L3 8 M3 3v5h5 M12 7v5l3 2",
  practice: "M12 2l3 7h7l-5.5 4 2 7-6.5-4.5L5.5 20l2-7L2 9h7z",
  settings: "M12 9a3 3 0 1 0 0 6 3 3 0 0 0 0-6z M19 12l2-1-2-4-2 1-2-1-1-2h-4l-1 2-2 1-2-1-2 4 2 1v0l-2 1 2 4 2-1 2 1 1 2h4l1-2 2-1 2 1 2-4z",
  arrow: "M5 12h14 M13 6l6 6-6 6", check: "M5 12l5 5 9-10", x: "M6 6l12 12M18 6L6 18", download: "M12 3v12 M7 10l5 5 5-5 M4 20h16", share: "M4 12v7h16v-7 M12 3v12 M7 8l5-5 5 5",
  heart: "M12 21s-8-5-8-11a4.5 4.5 0 0 1 8-3 4.5 4.5 0 0 1 8 3c0 6-8 11-8 11z", camera: "M4 7h4l2-3h4l2 3h4v13H4z M12 10a3.5 3.5 0 1 0 0 7 3.5 3.5 0 0 0 0-7z",
};
export function Icon({ name, size = 22, label }: { name: keyof typeof PATHS | string; size?: number; label?: string }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={1.9} strokeLinecap="round" strokeLinejoin="round" role={label ? "img" : undefined} aria-label={label} aria-hidden={label ? undefined : true}>
      <path d={PATHS[name] ?? ""} />
    </svg>
  );
}

export function Page({ title, subtitle, children, actions }: { title: string; subtitle?: string; children: ReactNode; actions?: ReactNode }) {
  const h = useRef<HTMLHeadingElement>(null);
  useEffect(() => { h.current?.focus(); }, [title]);
  return (
    <section className="fade-in mx-auto w-full max-w-5xl px-4 pb-28 pt-6 md:px-8 md:pb-10">
      <header className="mb-6 flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 ref={h} tabIndex={-1} className="text-2xl font-bold tracking-tight outline-none md:text-3xl">{title}</h1>
          {subtitle && <p className="muted mt-1">{subtitle}</p>}
        </div>
        {actions && <div className="flex flex-wrap gap-2">{actions}</div>}
      </header>
      {children}
    </section>
  );
}

/** Renders a pseudo-isochromatic plate as SVG (crisp at any size and printable). */
export function PlateView({ plate, filterId }: { plate: Plate; filterId?: string }) {
  return (
    <svg viewBox="0 0 1 1" className="aspect-square w-full max-w-[min(80vw,420px)] drop-shadow-xl" role="img" aria-label="Colour plate" style={filterId ? { filter: `url(#${filterId})` } : undefined}>
      <circle cx={0.5} cy={0.5} r={0.495} fill="#e9e4da" />
      {plate.dots.map((d, i) => <circle key={i} cx={d.x} cy={d.y} r={d.r} fill={d.color} />)}
    </svg>
  );
}

/** Hidden SVG defs exposing cvd-<type>-<sim|fix> filters at the given severity, usable on any element. */
export function CvdFilterDefs({ severity }: { severity: number }) {
  return (
    <svg width="0" height="0" className="absolute" aria-hidden>
      <defs>
        {CVD_TYPES.flatMap((t) => [
          <filter key={t + "s"} id={`cvd-${t}-sim`} colorInterpolationFilters="linearRGB"><feColorMatrix type="matrix" values={svgFilterValues(simulationMatrix(t, severity))} /></filter>,
          <filter key={t + "f"} id={`cvd-${t}-fix`} colorInterpolationFilters="linearRGB"><feColorMatrix type="matrix" values={svgFilterValues(correctionMatrix(t, severity))} /></filter>,
        ])}
      </defs>
    </svg>
  );
}

export interface ViewFilter { type: CvdType | null; correct: boolean; severity: number }
export const filterId = (f: ViewFilter) => (f.type ? `cvd-${f.type}-${f.correct ? "fix" : "sim"}` : undefined);

export function FilterControls({ value, onChange }: { value: ViewFilter; onChange: (f: ViewFilter) => void }) {
  const { t } = useApp();
  return (
    <div className="flex flex-col gap-3">
      <div className="flex flex-wrap gap-2" role="group" aria-label={t("filter_simulate")}>
        <button className="chip" aria-pressed={value.type === null} onClick={() => onChange({ ...value, type: null })}>{t("type_none")}</button>
        {CVD_TYPES.map((ty) => <button key={ty} className="chip" aria-pressed={value.type === ty} onClick={() => onChange({ ...value, type: ty })}>{t(`type_${ty}`)}</button>)}
      </div>
      <div className="flex flex-wrap gap-2">
        <button className="chip" aria-pressed={!value.correct} onClick={() => onChange({ ...value, correct: false })}>{t("filter_simulate")}</button>
        <button className="chip" aria-pressed={value.correct} onClick={() => onChange({ ...value, correct: true })}>{t("filter_correct")}</button>
      </div>
      <label className="text-sm">
        {t("filter_severity", { n: Math.round(value.severity * 100) })}
        <input type="range" min={0} max={1} step={0.05} value={value.severity} onChange={(e) => onChange({ ...value, severity: +e.target.value })} />
      </label>
    </div>
  );
}

export function Stat({ label, value, tone }: { label: string; value: ReactNode; tone?: "ok" | "warn" | "bad" }) {
  const c = tone === "ok" ? "text-ok" : tone === "warn" ? "text-warn" : tone === "bad" ? "text-bad" : "";
  return <div className="card p-4"><div className="muted text-xs uppercase tracking-wide">{label}</div><div className={`mt-1 text-xl font-bold ${c}`}>{value}</div></div>;
}

export function Toggle({ label, checked, onChange, hint }: { label: string; checked: boolean; onChange: (v: boolean) => void; hint?: string }) {
  return (
    <label className="flex min-h-12 cursor-pointer items-center justify-between gap-4 py-2">
      <span><span className="font-medium">{label}</span>{hint && <span className="muted block text-sm">{hint}</span>}</span>
      <input type="checkbox" role="switch" className="h-6 w-11 cursor-pointer appearance-none rounded-full bg-[var(--line)] transition before:block before:h-5 before:w-5 before:translate-x-0.5 before:translate-y-0.5 before:rounded-full before:bg-white before:transition checked:bg-brand checked:before:translate-x-5.5" checked={checked} onChange={(e) => onChange(e.target.checked)} />
    </label>
  );
}

/** Small live region for screen reader announcements. */
export function Announce({ text }: { text: string }) { return <p aria-live="polite" className="sr-only">{text}</p>; }
