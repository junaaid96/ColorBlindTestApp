import type { Classification, PlateOutcome, Severity } from "./scoring";

export type TestKind = "quick" | "full" | "arrangement" | "contrast" | "practice";
/** One saved result. Stored on this device only. */
export interface HistoryEntry {
  id: string;
  timestamp: number;
  kind: TestKind;
  classification: Classification | null;
  severity: Severity | null;
  /** 0..100 for plates/practice, C-index x100 for arrangement, log CS x100 for contrast. */
  score: number;
  confidence: number;
  details?: PlateOutcome[] | Record<string, number | string | boolean | null>;
}
export type ThemeMode = "system" | "light" | "dark";
export type Locale = "en" | "bn" | "es" | "hi";
export interface Settings {
  onboarded: boolean;
  theme: ThemeMode;
  highContrast: boolean;
  reduceMotion: boolean;
  speech: boolean;
  haptics: boolean;
  notifications: boolean;
  locale: Locale;
  calibrated: boolean;
}
export const DEFAULT_SETTINGS: Settings = {
  onboarded: false, theme: "system", highContrast: false, reduceMotion: false, speech: false,
  haptics: true, notifications: false, locale: "en", calibrated: false,
};
