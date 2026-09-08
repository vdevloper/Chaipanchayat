# Chai Panchayat — Product Requirements

## Product
A premium, editorial Indian digital news mobile app that surfaces stories from
`chaipanchayat.com` (WordPress REST API). The experience should feel like a real Indian
newspaper: serif headlines, restrained saffron accents, generous whitespace, flat cards.

## Brand
- Name: **Chai Panchayat** (चाय पंचायत)
- Primary accent: **Chai Saffron `#E85D04`** (used only as accent, never as full background)
- Warm editorial light background `#FFFDF8`, cards `#FFFFFF`
- Breaking-news accent `#C1121F`
- Dark mode: `#101010` background, `#1A1A1A` surface, saffron unchanged
- Typography: Noto Serif Devanagari (headlines, weights 400/700/800) + Inter (UI/body,
  400/500/600/700). Devanagari-capable so Hindi content renders correctly.

## Screens implemented
1. **Home (`/(tabs)/index.tsx`)** — Logo header + search/bell, horizontal category chip
   strip driven by WordPress, 16:9 hero card with gradient scrim, "Latest News" section,
   horizontal IMAGE|TEXT news cards, pull-to-refresh, "N new stories" pill on refresh.
2. **Categories (`/(tabs)/categories.tsx`)** — 2-column grid of every WordPress
   category with story counts, filter search input.
3. **Saved (`/(tabs)/saved.tsx`)** — Locally bookmarked articles via AsyncStorage.
4. **Settings (`/(tabs)/settings.tsx`)** — Text size preference (small/medium/large),
   appearance banner (follows system), about links.
5. **Article detail (`/article/[id].tsx`)** — Category kicker, serif headline, date,
   featured image, WordPress `content.rendered` parsed and rendered natively
   (paragraphs, headings, bold/italic, links, blockquotes, lists, images), sticky
   Save/Text-size/Share action bar. Share sends the original WordPress URL.
6. **Category feed (`/category/[id].tsx`)** — Feed for a chosen category.
7. **Search (`/search.tsx`)** — Debounced WordPress search with results count and empty
   state.

## Reusable pieces (in `src/`)
- `theme.ts` — full light + dark tokens, font families, type scale, spacing, radius.
- `api/wordpress.ts` — REST client (posts, categories, search, embed helpers, HTML
  entity decoder, tag stripper).
- `api/bookmarks.ts` — AsyncStorage bookmarks + `useBookmarks`, `useIsBookmarked`.
- `api/settings.ts` — Persisted user settings (text size).
- `api/push.ts` — Push registration to backend relay.
- Components: `Logo`, `NewsCard`, `HeroCard`, `CategoryChips`, `Skeletons`,
  `ArticleHTML`, `EmptyState`, `OfflineBanner`, `NewStoriesPill`, `TopBar`.

## Backend
Minimal FastAPI at `/app/backend/server.py`:
- `GET /api/` — health.
- `POST /api/register-push` — relays to `POST /api/v1/push/users/register` on the
  Emergent push service using `EMERGENT_PUSH_KEY`.
- `POST /api/broadcast-test` — helper for QA to send a test push to every subscriber.
- Background task: polls WordPress every 10 minutes, pushes a notification to all
  registered devices when a newer article is detected.

## Data source
Everything comes from `https://chaipanchayat.com/wp-json/wp/v2` — posts, categories,
search, featured media. No local news database. WordPress is the single source of
truth.

## Push notifications
- Uses Emergent-managed push (SuprSend relay) via `expo-notifications`
  `getDevicePushTokenAsync`.
- Every device registers under a shared `chai_all_subscribers` user id so broadcasts
  fan out to all installs.
- Tap handlers registered at module scope in `app/_layout.tsx` (warm + cold-start).
- Android channel `default` created at module scope with MAX importance.
- Requires `google-services.json` for Android push delivery (provided by user at
  deploy time). Feature is verified only on native builds — not Expo Go.

## Reading experience
- Body: 17sp base, 1.6 line-height, adjustable by user in Settings (0.9× / 1.0× / 1.15×).
- Skeleton loaders (not spinners) with reanimated shimmer.
- Offline banner appears when WordPress can't be reached.

## Not yet implemented / intentional gaps
- No account/login (per user's brief — MVP).
- No offline article cache beyond React Query's in-memory cache (design allows for it;
  can be added if requested).
- Custom notification sounds not enabled.
