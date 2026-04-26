import { check } from "@tauri-apps/plugin-updater";
import { relaunch } from "@tauri-apps/plugin-process";
import { showInfo, showSuccess, showError } from "$lib/state/state.svelte";
import { launcherStore } from "$lib/state/state.svelte";

export async function checkForUpdates(silent = false) {
  try {
    const update = await check();

    if (!update) {
      if (!silent) showInfo("Actualizaciones", "Ya tenés la última versión.");
      return;
    }

    showInfo(
      `Update disponible: v${update.version}`,
      "Descargando en segundo plano...",
    );

    let downloaded = 0;
    let total = 0;

    await update.download((event) => {
      switch (event.event) {
        case "Started":
          total = event.data.contentLength ?? 0;
          break;
        case "Progress":
          downloaded += event.data.chunkLength;
          const pct = total ? Math.round((downloaded / total) * 100) : 0;
          launcherStore.updateProgress = pct;
          break;
        case "Finished":
          launcherStore.updateProgress = 100;
          break;
      }
    });

    showSuccess("Update listo", "La actualización se instalará al reiniciar.");

    await update.install();
    await relaunch();
  } catch (err) {
    showError("Error de actualización", `${err}`);
  }
}
