import { launcherStore } from "$lib/state/state.svelte";
import es from "./es.json";
import en from "./en.json";

type DictValue = string | { [key: string]: DictValue };
const dicts: Record<string, DictValue> = { es, en };

export function t(
	key: string,
	params?: Record<string, string | number>,
): string {
	const lang = launcherStore.settings?.language || "es";
	const dict = dicts[lang] || dicts["es"];
	if (!dict || typeof dict === "string") return key;
	let value: DictValue = dict;
	for (const k of key.split(".")) {
		if (typeof value === "string") return key;
		value = value[k];
		if (value === undefined) return key;
	}
	const result = typeof value === "string" ? value : key;
	if (!params) return result;
	return result.replace(/\{(\w+)\}/g, (_, name) =>
		String(params[name] ?? `{${name}}`),
	);
}
