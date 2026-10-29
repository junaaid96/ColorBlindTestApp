import { useEffect, useState } from "react";
import { useApp } from "../state";
import { Page, Stat } from "../components/ui";
import { oddHueRound, type OddHueRound } from "../lib/oddHue";
import { hex } from "../lib/color";
import { activeStreak, epochDay } from "../lib/streak";

const SECONDS = 60;
export default function Practice() {
  const { t, buzz, recordPractice, addResult, bestPractice, streak } = useApp();
  const [level, setLevel] = useState(0);
  const [lives, setLives] = useState(3);
  const [round, setRound] = useState<OddHueRound>(() => oddHueRound(0));
  const [left, setLeft] = useState(SECONDS);
  const [running, setRunning] = useState(false);
  const over = running && (left <= 0 || lives <= 0);

  useEffect(() => { if (!running || over) return; const id = setInterval(() => setLeft((s) => s - 1), 1000); return () => clearInterval(id); }, [running, over]);
  useEffect(() => {
    if (!over) return;
    recordPractice(level);
    addResult({ kind: "practice", classification: null, severity: null, score: level, confidence: 100 });
    buzz("success");
  }, [over]); // eslint-disable-line react-hooks/exhaustive-deps

  const startGame = () => { setLevel(0); setLives(3); setLeft(SECONDS); setRound(oddHueRound(0)); setRunning(true); };
  const pick = (i: number) => {
    if (over) return;
    if (i === round.oddIndex) { buzz("tap"); const n = level + 1; setLevel(n); setRound(oddHueRound(n)); }
    else { buzz("error"); setLives((l) => l - 1); }
  };

  return (
    <Page title={t("practice_title")} subtitle={t("practice_desc")} actions={<button className="btn btn-primary" onClick={startGame}>{running ? t("retry") : t("start")}</button>}>
      <div className="mb-6 grid grid-cols-2 gap-3 md:grid-cols-4">
        <Stat label={t("practice_time")} value={`${Math.max(0, left)}s`} />
        <Stat label={t("practice_lives")} value={"♥".repeat(Math.max(0, lives)) || "—"} tone="bad" />
        <Stat label={t("practice_best")} value={bestPractice} />
        <Stat label={t("practice_streak")} value={`🔥 ${activeStreak(streak, epochDay())}`} />
      </div>
      {running && !over && (
        <div className="mx-auto grid max-w-md gap-1.5" style={{ gridTemplateColumns: `repeat(${round.size}, 1fr)` }} role="grid" aria-label={t("practice_level", { n: level + 1 })}>
          {Array.from({ length: round.size ** 2 }, (_, i) => (
            <button key={`${level}-${i}`} className="aspect-square rounded-xl transition hover:scale-[1.04]" style={{ background: hex(i === round.oddIndex ? round.odd : round.base) }} onClick={() => pick(i)} aria-label={`Tile ${i + 1}`} />
          ))}
        </div>
      )}
      {over && <div className="card p-8 text-center"><h2 className="text-2xl font-bold">{t("practice_over")}</h2><p className="mt-2 text-lg">{t("practice_level", { n: level })}</p></div>}
    </Page>
  );
}
