// Thin WordPress REST client for chaipanchayat.com.

const WP_BASE = "https://chaipanchayat.com/wp-json/wp/v2";

export type WPImage = { source_url: string; alt_text?: string } | undefined;

export interface WPPost {
  id: number;
  date: string; // ISO
  link: string;
  slug: string;
  title: { rendered: string };
  excerpt: { rendered: string };
  content: { rendered: string };
  categories: number[];
  _embedded?: {
    "wp:featuredmedia"?: Array<{ source_url: string; alt_text?: string }>;
    "wp:term"?: Array<Array<{ id: number; name: string; taxonomy: string }>>;
    author?: Array<{ name: string }>;
  };
}

export interface WPCategory {
  id: number;
  name: string;
  slug: string;
  count: number;
  description?: string;
}

async function wpGet<T>(path: string, params: Record<string, any> = {}): Promise<T> {
  const q = new URLSearchParams();
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null) q.append(k, String(v));
  });
  const url = `${WP_BASE}${path}?${q.toString()}`;
  const res = await fetch(url);
  if (!res.ok) {
    throw new Error(`WordPress ${res.status}: ${await res.text().catch(() => "")}`);
  }
  return res.json();
}

export function fetchPosts(opts: {
  page?: number;
  perPage?: number;
  categoryId?: number;
  search?: string;
} = {}): Promise<WPPost[]> {
  return wpGet<WPPost[]>("/posts", {
    per_page: opts.perPage ?? 20,
    page: opts.page ?? 1,
    orderby: "date",
    order: "desc",
    _embed: 1,
    ...(opts.categoryId ? { categories: opts.categoryId } : {}),
    ...(opts.search ? { search: opts.search } : {}),
  });
}

export function fetchPost(id: number | string): Promise<WPPost> {
  return wpGet<WPPost>(`/posts/${id}`, { _embed: 1 });
}

export function fetchCategories(): Promise<WPCategory[]> {
  return wpGet<WPCategory[]>("/categories", {
    per_page: 100,
    orderby: "count",
    order: "desc",
    hide_empty: 1,
  });
}

export function featuredImage(post: WPPost): string | undefined {
  return post._embedded?.["wp:featuredmedia"]?.[0]?.source_url;
}

export function primaryCategoryName(post: WPPost): string | undefined {
  const terms = post._embedded?.["wp:term"] ?? [];
  for (const group of terms) {
    for (const term of group) {
      if (term.taxonomy === "category") return term.name;
    }
  }
  return undefined;
}

/** Strip HTML tags and decode a few common entities. */
export function stripHtml(html: string): string {
  return decodeEntities(html.replace(/<[^>]+>/g, "")).replace(/\s+/g, " ").trim();
}

export function decodeEntities(text: string): string {
  return text
    .replace(/&nbsp;/g, " ")
    .replace(/&amp;/g, "&")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">")
    .replace(/&quot;/g, '"')
    .replace(/&#8217;/g, "\u2019")
    .replace(/&#8216;/g, "\u2018")
    .replace(/&#8220;/g, "\u201C")
    .replace(/&#8221;/g, "\u201D")
    .replace(/&#8211;/g, "\u2013")
    .replace(/&#8230;/g, "\u2026")
    .replace(/&#039;/g, "'")
    .replace(/&#(\d+);/g, (_, n) => String.fromCharCode(Number(n)))
    .replace(/&hellip;/g, "\u2026");
}
