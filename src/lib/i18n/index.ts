import { launcherStore } from "$lib/state/state.svelte";

const dicts: Record<string, any> = {};

async function loadDicts() {
    const [es, en] = await Promise.all([
        import("./es.json"),
        import("./en.json"),
    ]);
    dicts["es"] = es.default || es;
    dicts["en"] = en.default || en;
}

loadDicts();

export function t(key: string): string {
    const lang = launcherStore.settings?.language || "es";

    const dict = dicts[lang] || dicts["es"];
    if (!dict) return key;

    const value = key.split('.').reduce((obj, k) => (obj ? obj[k] : undefined), dict);
    return value ? value : key;
}
