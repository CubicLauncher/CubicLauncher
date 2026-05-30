import { launcherStore } from "$lib/state/state.svelte";
import es from "./es.json";
import en from "./en.json";

const dicts = { es, en };

export function t(key: string): string {
	const lang = launcherStore.settings?.language || "es";
	const dict = dicts[lang] || dicts["es"];
	if (!dict) return key;
	const value = key
		.split(".")
		.reduce((obj, k) => (obj ? obj[k] : undefined), dict);
	return value ? value : key;
}
