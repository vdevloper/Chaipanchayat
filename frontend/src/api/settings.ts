// Persisted app settings: text size + optional theme override.
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useCallback, useEffect, useState } from "react";

const KEY = "chai_settings_v1";

export type TextSize = "small" | "medium" | "large";

export interface Settings {
  textSize: TextSize;
}

const DEFAULTS: Settings = { textSize: "medium" };

type Listener = (s: Settings) => void;
const listeners = new Set<Listener>();

let cache: Settings | null = null;

async function load(): Promise<Settings> {
  if (cache) return cache;
  try {
    const raw = await AsyncStorage.getItem(KEY);
    cache = raw ? { ...DEFAULTS, ...JSON.parse(raw) } : DEFAULTS;
  } catch {
    cache = DEFAULTS;
  }
  return cache!;
}

async function save(next: Settings): Promise<void> {
  cache = next;
  await AsyncStorage.setItem(KEY, JSON.stringify(next));
  listeners.forEach((fn) => fn(next));
}

export function useSettings() {
  const [settings, setSettings] = useState<Settings>(cache ?? DEFAULTS);

  useEffect(() => {
    load().then(setSettings);
    const fn = (s: Settings) => setSettings(s);
    listeners.add(fn);
    return () => {
      listeners.delete(fn);
    };
  }, []);

  const update = useCallback(async (patch: Partial<Settings>) => {
    const current = await load();
    await save({ ...current, ...patch });
  }, []);

  return { settings, update };
}

export function textSizeMultiplier(size: TextSize): number {
  return size === "small" ? 0.9 : size === "large" ? 1.15 : 1.0;
}
