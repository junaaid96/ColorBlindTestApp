import { useEffect, useState } from "react";
import { useApp } from "../state";
import { Page, Toggle } from "../components/ui";
import { LOCALES } from "../i18n";
import type { Locale, ThemeMode } from "../lib/models";
import { appInfo, askConfirm, checkForUpdate, isTauri, openExternal } from "../platform";

const REPO = "https://github.com/junaaid96/color-vision-lab";
export default function Settings() {
  const { t, settings: s, update } = useApp();
  const [info, setInfo] = useState<Awaited<ReturnType<typeof appInfo>> | null>(null);
  const [upd, setUpd] = useState("");
  useEffect(() => { appInfo().then(setInfo); }, []);
  const desktop = info && ["macos", "windows", "linux"].includes(info.os);

  const check = async () => {
    setUpd("…");
    try {
      const u = await checkForUpdate();
      if (!u) { setUpd(t("set_uptodate")); return; }
      setUpd(t("set_update_avail", { v: u.version }));
      if (await askConfirm(t("set_update_avail", { v: u.version }))) await u.install();
    } catch (e) { setUpd(String(e)); }
  };

  return (
    <Page title={t("nav_settings")}>
      <div className="grid gap-6 lg:grid-cols-2">
        <section className="card flex flex-col gap-4 p-5" aria-labelledby="look">
          <h2 id="look" className="font-semibold">{t("set_theme")}</h2>
          <div role="radiogroup" className="flex flex-wrap gap-2">
            {(["system", "light", "dark"] as ThemeMode[]).map((m) => (
              <button key={m} role="radio" aria-checked={s.theme === m} className="chip" aria-pressed={s.theme === m} onClick={() => update({ theme: m })}>{t(`set_${m}`)}</button>
            ))}
          </div>
          <Toggle label={t("set_high_contrast")} checked={s.highContrast} onChange={(v) => update({ highContrast: v })} />
          <Toggle label={t("set_reduce_motion")} checked={s.reduceMotion} onChange={(v) => update({ reduceMotion: v })} />
          <label className="flex items-center justify-between gap-3">{t("set_language")}
            <select className="btn" value={s.locale} onChange={(e) => update({ locale: e.target.value as Locale })}>
              {LOCALES.map((l) => <option key={l.id} value={l.id}>{l.label}</option>)}
            </select>
          </label>
        </section>
        <section className="card flex flex-col gap-4 p-5">
          <Toggle label={t("set_speech")} checked={s.speech} onChange={(v) => update({ speech: v })} />
          <Toggle label={t("set_haptics")} checked={s.haptics} onChange={(v) => update({ haptics: v })} />
          <Toggle label={t("set_notifications")} checked={s.notifications} onChange={(v) => update({ notifications: v })} />
          <button className="btn" onClick={() => update({ calibrated: false, onboarded: false })}>{t("cal_title")}</button>
        </section>
        <section className="card flex flex-col gap-3 p-5 lg:col-span-2">
          <h2 className="font-semibold">{t("set_about")}</h2>
          <p>{t("appName")} {info?.version} · {info?.os}{info?.arch ? ` (${info.arch})` : ""}</p>
          <p className="muted">{t("set_privacy")}</p>
          <p className="muted text-sm">{t("shortcut_help")}</p>
          <p className="muted text-sm">{t("disclaimer")}</p>
          <div className="flex flex-wrap gap-2">
            {isTauri() && desktop && <button className="btn" onClick={check}>{t("set_update")}</button>}
            <button className="btn" onClick={() => openExternal(REPO)}>GitHub</button>
          </div>
          <p aria-live="polite" className="text-sm">{upd}</p>
        </section>
      </div>
    </Page>
  );
}
