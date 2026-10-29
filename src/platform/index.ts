/**
 * Platform bridge. Every native capability is feature-detected so the same bundle runs as the
 * Tauri app (desktop + mobile) and as a plain static web app. Tauri modules are imported lazily
 * so the web build never touches them at runtime.
 */
import { isTauri as coreIsTauri } from "@tauri-apps/api/core";

export const isTauri = (): boolean => !__WEB_BUILD__ && coreIsTauri();

export type OsFamily = "macos" | "windows" | "linux" | "android" | "ios" | "web";
let osCache: OsFamily | null = null;
export async function osFamily(): Promise<OsFamily> {
  if (osCache) return osCache;
  if (!isTauri()) return (osCache = "web");
  const { platform } = await import("@tauri-apps/plugin-os");
  const p = platform();
  osCache = (["macos", "windows", "linux", "android", "ios"] as const).find((x) => x === p) ?? "linux";
  return osCache;
}
export const isMobile = async () => ["android", "ios"].includes(await osFamily());

/* ---------- key/value storage: plugin-store or localStorage ---------- */
type StoreLike = { get<T>(k: string): Promise<T | undefined>; set(k: string, v: unknown): Promise<void> };
let storePromise: Promise<StoreLike> | null = null;
function store(): Promise<StoreLike> {
  if (!storePromise) {
    storePromise = isTauri()
      ? import("@tauri-apps/plugin-store").then(({ load }) => load("color-vision-lab.json", { defaults: {}, autoSave: 200 }) as Promise<StoreLike>)
      : Promise.resolve<StoreLike>({
          async get<T>(k: string) { const v = localStorage.getItem(`cvl:${k}`); return v == null ? undefined : (JSON.parse(v) as T); },
          async set(k, v) { localStorage.setItem(`cvl:${k}`, JSON.stringify(v)); },
        });
  }
  return storePromise;
}
export const kvGet = async <T,>(k: string) => (await store()).get<T>(k);
export const kvSet = async (k: string, v: unknown) => (await store()).set(k, v);

/* ---------- files ---------- */
/** Save text or bytes. Native save dialog + fs plugin in Tauri, browser download on the web. */
export async function saveFile(name: string, content: string | Uint8Array, mime: string): Promise<boolean> {
  if (isTauri()) {
    const { save } = await import("@tauri-apps/plugin-dialog");
    const ext = name.split(".").pop() ?? "";
    const path = await save({ defaultPath: name, filters: [{ name: ext.toUpperCase(), extensions: [ext] }] });
    if (!path) return false;
    const fs = await import("@tauri-apps/plugin-fs");
    if (typeof content === "string") await fs.writeTextFile(path, content);
    else await fs.writeFile(path, content);
    return true;
  }
  const blob = new Blob([content as BlobPart], { type: mime });
  const url = URL.createObjectURL(blob);
  const a = Object.assign(document.createElement("a"), { href: url, download: name });
  document.body.append(a); a.click(); a.remove();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
  return true;
}

/** Pick a text file (backup import). */
export function pickTextFile(accept: string): Promise<string | null> {
  return new Promise((resolve) => {
    const input = Object.assign(document.createElement("input"), { type: "file", accept });
    input.onchange = async () => resolve(input.files?.[0] ? await input.files[0].text() : null);
    input.oncancel = () => resolve(null);
    input.click();
  });
}

/* ---------- feedback ---------- */
export async function haptic(kind: "success" | "error" | "tap" = "tap") {
  try {
    if (isTauri() && (await isMobile())) {
      const h = await import("@tauri-apps/plugin-haptics");
      if (kind === "tap") await h.selectionFeedback();
      else await h.notificationFeedback(kind === "success" ? "success" : "error");
    } else navigator.vibrate?.(kind === "tap" ? 10 : kind === "success" ? [15, 40, 15] : 60);
  } catch { /* haptics are best effort */ }
}

export async function notify(title: string, body: string) {
  try {
    if (isTauri()) {
      const n = await import("@tauri-apps/plugin-notification");
      let ok = await n.isPermissionGranted();
      if (!ok) ok = (await n.requestPermission()) === "granted";
      if (ok) n.sendNotification({ title, body });
    } else if ("Notification" in window) {
      if (Notification.permission === "default") await Notification.requestPermission();
      if (Notification.permission === "granted") new Notification(title, { body });
    }
  } catch { /* optional */ }
}

export async function copyText(text: string) {
  if (isTauri()) { const { writeText } = await import("@tauri-apps/plugin-clipboard-manager"); await writeText(text); }
  else await navigator.clipboard.writeText(text);
}

export async function openExternal(url: string) {
  if (isTauri()) { const { openUrl } = await import("@tauri-apps/plugin-opener"); await openUrl(url); }
  else window.open(url, "_blank", "noopener,noreferrer");
}

export async function share(title: string, text: string): Promise<"shared" | "copied"> {
  if (navigator.share) { try { await navigator.share({ title, text }); return "shared"; } catch { /* fall back */ } }
  await copyText(text);
  return "copied";
}

export function speak(text: string, lang: string) {
  if (!("speechSynthesis" in window)) return;
  speechSynthesis.cancel();
  const u = new SpeechSynthesisUtterance(text); u.lang = lang;
  speechSynthesis.speak(u);
}

export async function log(level: "info" | "warn" | "error", msg: string) {
  if (isTauri()) { const l = await import("@tauri-apps/plugin-log"); await l[level](msg); }
  else console[level](msg);
}

/* ---------- Rust image pipeline ---------- */
/**
 * Runs the Machado/daltonize matrix over RGBA pixels in Rust (raw IPC bytes, no JSON) when
 * running in Tauri; returns null on the web so callers use the JS implementation.
 */
export async function nativeFilterPixels(rgba: Uint8ClampedArray, mode: string, severity: number): Promise<Uint8Array | null> {
  if (!isTauri()) return null;
  const { invoke } = await import("@tauri-apps/api/core");
  const out = await invoke<ArrayBuffer>("filter_pixels", new Uint8Array(rgba.buffer, rgba.byteOffset, rgba.byteLength), {
    headers: { "x-mode": mode, "x-severity": String(severity) },
  });
  return new Uint8Array(out);
}

export async function appInfo(): Promise<{ version: string; os: OsFamily; arch?: string; tauri?: string }> {
  if (!isTauri()) return { version: import.meta.env.VITE_APP_VERSION ?? "3.0.0", os: "web" };
  const { getVersion, getTauriVersion } = await import("@tauri-apps/api/app");
  const { arch } = await import("@tauri-apps/plugin-os");
  return { version: await getVersion(), tauri: await getTauriVersion(), os: await osFamily(), arch: arch() };
}

/* ---------- updater (desktop only) ---------- */
export async function checkForUpdate(): Promise<{ version: string; install: () => Promise<void> } | null> {
  if (!isTauri() || (await isMobile())) return null;
  const { check } = await import("@tauri-apps/plugin-updater");
  const update = await check();
  if (!update) return null;
  return {
    version: update.version,
    install: async () => {
      await update.downloadAndInstall();
      const { relaunch } = await import("@tauri-apps/plugin-process");
      await relaunch();
    },
  };
}

/* ---------- deep links: colorvisionlab://<route> ---------- */
export async function onDeepLink(cb: (route: string) => void): Promise<() => void> {
  if (!isTauri()) return () => {};
  const { onOpenUrl, getCurrent } = await import("@tauri-apps/plugin-deep-link");
  const handle = (urls: string[] | null) => {
    const u = urls?.[0];
    if (u) cb(u.replace(/^colorvisionlab:\/\//, "").replace(/\/$/, ""));
  };
  handle(await getCurrent().catch(() => null));
  return onOpenUrl(handle);
}

/** Native confirm dialog in Tauri (window.confirm is unreliable in some webviews). */
export async function askConfirm(message: string): Promise<boolean> {
  if (isTauri()) { const { confirm } = await import("@tauri-apps/plugin-dialog"); return confirm(message, { kind: "warning" }); }
  return window.confirm(message);
}
