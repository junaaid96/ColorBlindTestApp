import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import App from "./App";
import { AppProvider } from "./state";

createRoot(document.getElementById("root")!).render(<StrictMode><AppProvider><App /></AppProvider></StrictMode>);

// Offline support for the web build only (the native app bundles all assets).
if (__WEB_BUILD__ && "serviceWorker" in navigator && import.meta.env.PROD) {
  addEventListener("load", () => navigator.serviceWorker.register("./sw.js").catch(() => {}));
}
