import { launcherStore } from "$lib/state/state.svelte";
import es from "./es.json";
import en from "./en.json";

const dicts: Record<string, any> = { es, en };

export function t(key: string): string {
    // Reactive mapping to the launcherStore
    const lang = launcherStore.settings?.language || "es";
    const dict = dicts[lang] || dicts["es"];
    
    // Safely parse "settings.tabs.general" -> dict["settings"]["tabs"]["general"]
    const value = key.split('.').reduce((obj, k) => (obj ? obj[k] : undefined), dict);
    return value ? value : key;
}
