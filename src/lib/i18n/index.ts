import { launcherStore } from "$lib/state/state.svelte";

const dicts: Record<string, any> = {};
const loaders: Record<string, () => Promise<any>> = {
    es: () => import("./es.json"),
    en: () => import("./en.json"),
};

let loading: Promise<any> | null = null;

async function ensureDict(lang: string): Promise<any> {
    if (!dicts[lang] && loaders[lang]) {
        if (!loading) {
            loading = loaders[lang]().then((m) => {
                dicts[lang] = m.default || m;
                loading = null;
            });
        }
        await loading;
    }
    return dicts[lang] || dicts["es"];
}

export function t(key: string): string {
    const lang = launcherStore.settings?.language || "es";

    if (!dicts[lang] && loaders[lang]) {
        ensureDict(lang);
    }

    const dict = dicts[lang] || dicts["es"];
    if (!dict) return key;

    const value = key.split('.').reduce((obj, k) => (obj ? obj[k] : undefined), dict);
    return value ? value : key;
}
