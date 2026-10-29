import { en, type Strings } from "./en";
import type { Locale } from "../lib/models";

/** Partial translations fall back to English per key; add keys here to translate more. */
const bn: Partial<Strings> = { appName: "কালার ভিশন ল্যাব", nav_home: "হোম", nav_tests: "পরীক্ষা", nav_lens: "লেন্স", nav_simulator: "সিমুলেটর", nav_history: "ইতিহাস", nav_practice: "অনুশীলন", nav_settings: "সেটিংস", start: "শুরু", next: "পরবর্তী", back: "পিছনে", done: "সম্পন্ন", plate_prompt: "আপনি কোন সংখ্যা দেখছেন?", plate_nothing: "কিছুই দেখছি না", result_title: "আপনার ফলাফল" };
const es: Partial<Strings> = { nav_home: "Inicio", nav_tests: "Pruebas", nav_lens: "Lente", nav_simulator: "Simulador", nav_history: "Historial", nav_practice: "Práctica", nav_settings: "Ajustes", start: "Empezar", next: "Siguiente", back: "Atrás", done: "Hecho", plate_prompt: "¿Qué número ves?", plate_nothing: "No veo nada", result_title: "Tu resultado", disclaimer: "Solo es una prueba de detección, no un diagnóstico médico. Si tienes dudas, consulta a un profesional de la vista." };
const hi: Partial<Strings> = { nav_home: "होम", nav_tests: "परीक्षण", nav_lens: "लेंस", nav_simulator: "सिम्युलेटर", nav_history: "इतिहास", nav_practice: "अभ्यास", nav_settings: "सेटिंग्स", start: "शुरू करें", next: "अगला", back: "पीछे", done: "हो गया", plate_prompt: "आपको कौन सी संख्या दिखती है?", plate_nothing: "कुछ नहीं दिखता", result_title: "आपका परिणाम" };

export const LOCALES: { id: Locale; label: string }[] = [
  { id: "en", label: "English" }, { id: "bn", label: "বাংলা" }, { id: "es", label: "Español" }, { id: "hi", label: "हिन्दी" },
];
const TABLE: Record<Locale, Partial<Strings>> = { en, bn, es, hi };

export type Key = keyof Strings;
export function translator(locale: Locale) {
  const t = TABLE[locale] ?? en;
  return (key: Key, vars?: Record<string, string | number>) => {
    let s = (t[key] ?? en[key]) as string;
    if (vars) for (const [k, v] of Object.entries(vars)) s = s.replace(`{${k}}`, String(v));
    return s;
  };
}
export type T = ReturnType<typeof translator>;
