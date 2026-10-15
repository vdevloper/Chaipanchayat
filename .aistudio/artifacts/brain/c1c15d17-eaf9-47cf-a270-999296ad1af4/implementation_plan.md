# Implementation Plan: Accurate Post Timings, Share Tagline Update & Instagram Reels Native Video Player

Revamp post timing calculations to accurately reflect publication timestamps without timezone drift, update the post sharing closing tagline to retain 'बेबाक, सटीक, निष्पक्ष' without 'खबरें', and redesign the Videos screen into a pure, full-screen Instagram Reels-style vertical feed powered by native WordPress MP4 video uploads.

## User Review Required

> [!IMPORTANT]
> - **Video Reels Experience**: Immersive full-screen vertical pager (`VerticalPager`) with tap-to-pause/play, auto-looping, and pure video playback free of comment sections or like clutter.
> - **Video Media Sources**: Both native WordPress uploaded media (`/wp-json/wp/v2/media?media_type=video`) and WordPress posts containing direct MP4 videos will be fetched and unified.
> - **Post Timings**: Accurate timestamp parsing recognizing Indian Standard Time (`Asia/Kolkata`, UTC+05:30) so times are never off by 5.5 hours or showing incorrect relative durations.
> - **Share Tagline**: Updated to `(चाय पंचायत - बेबाक, सटीक, निष्पक्ष)` without the word `खबरें`.

---

## Proposed Changes

### 1. Fix Post Timings (`DateUtils.kt` & `WordPressApiClient.kt`)

- **Root Cause**: WordPress REST API returns `date` in the site's local time (IST, `Asia/Kolkata`), but `DateUtils.parseDate` defaulted to UTC for formats without explicit offset. As a result, newly published posts were parsed 5.5 hours into the future, causing relative timings to fail or display incorrectly.
- **Solution**:
  - In `DateUtils.kt`, parse naive ISO dates using the Indian Standard Time timezone (`Asia/Kolkata` / GMT+05:30), while preserving ISO-8601 with timezone offsets (`XXX` / `Z`).
  - Add robust relative time logic ("अभी-अभी", "X मिनट पहले", "X घंटे पहले", "कल", "X दिन पहले") with clean Hindi/English formatting fallback.
  - In `WordPressApiClient.kt`, capture both `date` and `date_gmt` to ensure consistent epoch calculation.

---

### 2. Update Share Message Tagline (`DeepLinkManager.kt`)

- In `DeepLinkManager.buildShareMessage` and `buildHtmlShareMessage`:
  - Change the closing tagline from:
    `(चाय पंचायत - बेबाक, सटीक, निष्पक्ष खबरें)`
  - To:
    `(चाय पंचायत - बेबाक, सटीक, निष्पक्ष)`
  - Retaining `बेबाक, सटीक, निष्पक्ष` and removing `खबरें` as requested.

---

### 3. Native WordPress Video Ingestion (`WordPressApiClient.kt` & `NewsRepository.kt`)

- In `WordPressApiClient`:
  - Add native media query: Fetch `/wp-json/wp/v2/media?media_type=video&per_page=20` to directly load uploaded `.mp4` video files (`source_url`, `guid.rendered`, title, and date).
  - Enhance post video parsing: In addition to the media library, parse posts that contain direct HTML5 `<video src="...">`, `<source src="...">`, or direct uploaded `.mp4` URLs.
  - Combine both sources into unified `VideoItem` models prioritized by recency, setting `videoUrl` directly to the native MP4 stream.

---

### 4. Full-Screen Instagram Reels Feed (`VideosScreen.kt` & `ReelsVideoPlayer.kt`)

- Replace the multi-section layout (chips, hero card, article bottom sheets, comments, and like widgets) in `VideosScreen.kt` with a modern Instagram Reels vertical experience:
  - **Vertical Pager**: `VerticalPager` allowing users to swipe up and down smoothly between videos.
  - **Full-Screen Immersive Video**: Videos fill the entire screen with aspect ratio handling (aspect fit / fill).
  - **Tap to Pause / Play**: Single tap anywhere on the screen toggles playback with a subtle visual ripple and pause icon overlay.
  - **Auto-Play Active Video**: Only the currently visible page plays audio and video; swiping pauses the previous video and immediately buffers the next.
  - **Minimalist Overlay**: Pure video aesthetic with only a discreet bottom gradient featuring the headline and category, plus an optional share button, completely eliminating unnecessary comment sections and clutter.

---

## Verification Plan

### Automated Verification
- Run `compile_applet` to ensure Kotlin compilation, compose dependencies, and layout stability.
- Run unit/Robolectric tests for `DateUtils` to verify that an IST timestamp calculates the exact correct elapsed minutes and hours against system time.

### Manual / Device Verification
- **Timestamps**: Verify home feed, hero cards, news lists, and article detail show realistic, non-negative relative times matching current publication dates.
- **Share Message**: Verify sharing an article generates text ending in `(चाय पंचायत - बेबाक, सटीक, निष्पक्ष)`.
- **Reels Experience**: Open the Videos tab, verify full-screen vertical swipe, tap to pause/play, and verify native MP4 playback from WordPress uploads.
