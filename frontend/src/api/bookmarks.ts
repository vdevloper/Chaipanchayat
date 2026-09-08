// Local bookmark store using AsyncStorage.
import AsyncStorage from "@react-native-async-storage/async-storage";
import { useCallback, useEffect, useState } from "react";
import type { WPPost } from "@/src/api/wordpress";
import { featuredImage, primaryCategoryName, stripHtml } from "@/src/api/wordpress";

const KEY = "chai_bookmarks_v1";

export interface Bookmark {
  id: number;
  title: string;
  excerpt: string;
  link: string;
  date: string;
  image?: string;
  category?: string;
  savedAt: number;
}

async function readAll(): Promise<Bookmark[]> {
  try {
    const raw = await AsyncStorage.getItem(KEY);
    return raw ? (JSON.parse(raw) as Bookmark[]) : [];
  } catch {
    return [];
  }
}

async function writeAll(list: Bookmark[]): Promise<void> {
  await AsyncStorage.setItem(KEY, JSON.stringify(list));
}

export function toBookmark(post: WPPost): Bookmark {
  return {
    id: post.id,
    title: stripHtml(post.title.rendered),
    excerpt: stripHtml(post.excerpt.rendered),
    link: post.link,
    date: post.date,
    image: featuredImage(post),
    category: primaryCategoryName(post),
    savedAt: Date.now(),
  };
}

// Simple in-process pub/sub so multiple screens stay in sync.
type Listener = () => void;
const listeners = new Set<Listener>();
function emit() {
  listeners.forEach((fn) => fn());
}

export function useBookmarks() {
  const [list, setList] = useState<Bookmark[]>([]);
  const [ready, setReady] = useState(false);

  const refresh = useCallback(async () => {
    const items = await readAll();
    items.sort((a, b) => b.savedAt - a.savedAt);
    setList(items);
    setReady(true);
  }, []);

  useEffect(() => {
    refresh();
    const fn = () => refresh();
    listeners.add(fn);
    return () => {
      listeners.delete(fn);
    };
  }, [refresh]);

  return { bookmarks: list, ready };
}

export function useIsBookmarked(id: number | undefined) {
  const [saved, setSaved] = useState(false);
  useEffect(() => {
    let mounted = true;
    const check = async () => {
      if (!id) {
        setSaved(false);
        return;
      }
      const items = await readAll();
      if (mounted) setSaved(items.some((b) => b.id === id));
    };
    check();
    const fn = () => check();
    listeners.add(fn);
    return () => {
      mounted = false;
      listeners.delete(fn);
    };
  }, [id]);
  return saved;
}

export async function toggleBookmark(post: WPPost): Promise<boolean> {
  const items = await readAll();
  const exists = items.some((b) => b.id === post.id);
  const next = exists
    ? items.filter((b) => b.id !== post.id)
    : [toBookmark(post), ...items];
  await writeAll(next);
  emit();
  return !exists;
}

export async function removeBookmark(id: number): Promise<void> {
  const items = await readAll();
  await writeAll(items.filter((b) => b.id !== id));
  emit();
}
