//! Color Vision Lab — Tauri backend. All data stays on the device; the only network call is the
//! desktop updater checking GitHub Releases.

mod color;

use serde::Serialize;
use tauri::ipc::{InvokeBody, Request, Response};

/// Filters raw RGBA8 bytes. Body: the pixels. Headers: `x-mode` (`protan-sim`, `deutan-fix`, ...)
/// and `x-severity` (0..1). Raw IPC avoids JSON-encoding megabytes of pixels.
#[tauri::command]
async fn filter_pixels(request: Request<'_>) -> Result<Response, String> {
    let InvokeBody::Raw(bytes) = request.body() else {
        return Err("expected raw RGBA bytes".into());
    };
    if bytes.len() % 4 != 0 || bytes.len() > 64 * 1024 * 1024 {
        return Err("invalid pixel buffer".into());
    }
    let header = |k: &str| {
        request
            .headers()
            .get(k)
            .and_then(|v| v.to_str().ok())
            .map(str::to_owned)
    };
    let mode = header("x-mode").ok_or("missing x-mode")?;
    let severity: f32 = header("x-severity")
        .and_then(|s| s.parse().ok())
        .unwrap_or(1.0);
    if !severity.is_finite() {
        return Err("invalid severity".into());
    }
    let m = color::matrix_for_mode(&mode, severity)?;
    let mut out = bytes.clone();
    tauri::async_runtime::spawn_blocking(move || {
        color::apply(&mut out, &m);
        out
    })
    .await
    .map(Response::new)
    .map_err(|e| e.to_string())
}

#[derive(Serialize)]
struct AppInfo {
    version: String,
    arch: &'static str,
    tauri: &'static str,
}

#[tauri::command]
fn app_info(app: tauri::AppHandle) -> AppInfo {
    AppInfo {
        version: app.package_info().version.to_string(),
        arch: std::env::consts::ARCH,
        tauri: tauri::VERSION,
    }
}

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    let mut builder = tauri::Builder::default();

    #[cfg(desktop)]
    {
        // Must be registered first: a second launch (e.g. from a colorvisionlab:// link) focuses this one.
        builder = builder.plugin(tauri_plugin_single_instance::init(|app, _args, _cwd| {
            use tauri::Manager;
            if let Some(w) = app.get_webview_window("main") {
                let _ = w.unminimize();
                let _ = w.set_focus();
            }
        }));
        builder = builder
            .plugin(tauri_plugin_window_state::Builder::default().build())
            .plugin(tauri_plugin_updater::Builder::new().build())
            .plugin(tauri_plugin_process::init());
    }
    #[cfg(mobile)]
    {
        builder = builder.plugin(tauri_plugin_haptics::init());
    }

    builder
        .plugin(
            tauri_plugin_log::Builder::new()
                .level(if cfg!(debug_assertions) {
                    log::LevelFilter::Debug
                } else {
                    log::LevelFilter::Info
                })
                .build(),
        )
        .plugin(tauri_plugin_deep_link::init())
        .plugin(tauri_plugin_store::Builder::default().build())
        .plugin(tauri_plugin_fs::init())
        .plugin(tauri_plugin_dialog::init())
        .plugin(tauri_plugin_os::init())
        .plugin(tauri_plugin_opener::init())
        .plugin(tauri_plugin_notification::init())
        .plugin(tauri_plugin_clipboard_manager::init())
        .setup(|_app| {
            #[cfg(all(desktop, any(windows, target_os = "linux")))]
            {
                use tauri_plugin_deep_link::DeepLinkExt;
                // Register the scheme at runtime for dev/portable builds; installers also register it.
                let _ = _app.deep_link().register_all();
            }
            Ok(())
        })
        .invoke_handler(tauri::generate_handler![filter_pixels, app_info])
        .run(tauri::generate_context!())
        .expect("error while running Color Vision Lab");
}
