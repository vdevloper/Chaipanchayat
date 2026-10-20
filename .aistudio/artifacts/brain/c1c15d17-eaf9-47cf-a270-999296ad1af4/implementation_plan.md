# Chai Panchayat Experience Refinements & Editorial Polish

Refine the digital news app with an immersive unified video feed, warm editorial ivory lighting in light mode, complete inline post image gallery rendering, de-duplicated in-flow video playback with autoplay, and updated Flash News branding.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following user preferences were confirmed during clarification and will be implemented:
> - **Light Mode Aesthetics**: Warm Editorial Ivory (`#FBF9F5` / `#F8F5EE`) with soft card drop shadows, ambient card lighting, and high-contrast borders.
> - **In-Post Video Playback**: Single embedded player placed naturally within the article's text flow with smooth autoplay, completely eliminating duplicate top video reports.
> - **Article Image Gallery**: High-resolution inline figures with rounded corners, responsive aspect ratios, and genuine WordPress captions.
> - **Video Feed Streamlining**: Removal of the top "Taja Chai" filter tab to provide a pure, unified, full-screen vertical video feed.
> - **Live Ticker Branding**: Replacing the "LATEST" badge with "Flash News" and active pulse indicator.

---

## 1. Overview & Core Concept

- **What It Does**: Enhances visual depth, eliminates redundancy, and fixes media rendering across the app. Light mode shifts from stark, flat white to a rich, warm editorial newspaper aesthetic with ambient card lighting. The video feed becomes an uninterrupted vertical video stream. Articles render full multi-photo galleries with captions and play embedded videos seamlessly without duplicates.
- **Target Audience**: Hindi and regional news readers seeking a fast, authentic, and visually inviting mobile reading experience.
- **Key Value**: Restores visual hierarchy and glass effects in light theme, ensures every picture published on WordPress is visible, and delivers smooth, non-redundant media playback.

---

## 2. User Experience & Visual Design

### Key User Flows
1. **Home Feed & Flash News**: Readers launch the app to see a warm ivory newspaper backdrop. The live headline ticker immediately greets them with a vibrant crimson **Flash News** badge cycling Breaking News. Cards have soft, tangible depth with warm lighting instead of flat white.
2. **Reading Posts with Full Galleries**: Opening an article displays the headline, author byline, and hero picture. Scrolling through the story reveals all inline WordPress photos and photo essay figures with styled captions and rounded corners.
3. **In-Flow Video Watching**: Articles featuring video reports embed a single, hardware-accelerated player directly in the narrative flow that autoplays smoothly without jumping or duplicating at the top.
4. **Pure Video Section**: Swiping to the Videos tab launches full-screen vertical news videos without extra segmented filter tabs or distractions.

### Visual Identity & Theme Tokens
- **Backdrop & Surfaces (Light Theme)**:
  - Surface Primary: `#FBF9F5` (Warm Editorial Ivory)
  - Surface Secondary (Cards & Elevated Panels): `#FFFFFF` with warm border `#E7E0D6` and ambient soft shadow (`0x0A201A0E`)
  - Accent Gradient: `#FF5A00` (Chai Saffron) to `#E52B2B` (Breaking Crimson)
- **Typography**: Noto Serif Devanagari for headlines with crisp Inter numerals and metadata.
- **Component Styling**:
  - News cards feature subtle 1.5dp elevation with soft ambient shadows.
  - In-article images feature 10dp rounded corners, subtle border, and styled italic caption blocks.

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Removal of 'Taja Chai' Segmented Tab in Videos Screen**
  - *Chosen Approach*: Deliver a single, unified vertical video feed ordered chronologically by authentic post timings.
  - *Why*: Eliminates user confusion, maximizes vertical video real estate, and removes redundant filtering.
- **Decision 2: Warm Editorial Ivory vs. Pure Flat White in Light Mode**
  - *Chosen Approach*: Update light theme tokens to warm ivory paper tones with subtle elevation and borders.
  - *Why*: Prevents glass cards, skeleton loaders, and dividers from washing out or becoming invisible on bright screens.
- **Decision 3: Natural In-Flow Video Placement vs. Top Banner**
  - *Chosen Approach*: Render the in-post video player exclusively within the article content blocks where the journalist placed it.
  - *Why*: Removes awkward duplicate video players (previously rendered both above the text and inside the text) and preserves editorial flow.
- **Decision 4: Enhanced HTML Parser for Post Images & Captions**
  - *Chosen Approach*: Upgrade `ArticleHtmlView` to parse Gutenberg `<figure>`, `<figcaption>`, and inline `<p><img/></p>` tags, supporting lazy-loaded `data-src` attributes.
  - *Why*: Ensures photos in multi-image reports and photo stories render reliably rather than being stripped.

---

## 4. Technical Architecture & Data Strategy

```
┌────────────────────────────────────────────────────────┐
│                      HomeScreen                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │ BreakingNewsTicker: "Flash News" [Pulsing Badge] │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ NewsCard (Warm Ivory Surface + Soft Card Shadow) │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
                           │
                           ▼
┌────────────────────────────────────────────────────────┐
│                     ArticleScreen                      │
│  ┌──────────────────────────────────────────────────┐  │
│  │ Lead Header Image + Meta Byline                  │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ArticleHtmlView (Single In-Flow Video + Images)  │  │
│  │   ├─ HtmlBlock.Paragraph                         │  │
│  │   ├─ HtmlBlock.ImageBlock (Captions + HD Photos) │  │
│  │   ├─ HtmlBlock.VideoEmbed (ExoPlayer Autoplay)   │  │
│  │   └─ HtmlBlock.Paragraph                         │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
```

### Components to Modify:
1. `app/src/main/java/com/chaipanchayat/app/ui/screens/VideosScreen.kt`:
   - Remove `VideoFeedTab` enum and segmented tabs pill ("वीडियो" / "ताज़ा चाय").
   - Display a single, continuous video pager with authentic post timings.
2. `app/src/main/java/com/chaipanchayat/app/ui/theme/Color.kt` & `Theme.kt`:
   - Update `LightSurface` to `#FBF9F5`, `LightSurfaceSecondary` to `#FFFFFF`, `LightSurfaceTertiary` to `#F4EFEB`, and add soft card shadow and ambient lighting borders.
3. `app/src/main/java/com/chaipanchayat/app/ui/components/ArticleHtmlView.kt`:
   - Support Gutenberg `<figure>` blocks and extract `<figcaption>` for genuine editorial captions.
   - Extract `<img>` tags inside `<p>` blocks.
   - Support `data-src` / `data-orig-file` for lazy-loaded WordPress pictures.
4. `app/src/main/java/com/chaipanchayat/app/ui/screens/ArticleScreen.kt`:
   - Remove duplicate top `ChaiVideoPlayer` block.
   - Pass `autoPlay = true` to in-body `ChaiVideoPlayer` when encountered in article text flow.
5. `app/src/main/java/com/chaipanchayat/app/ui/components/BreakingNewsTicker.kt`:
   - Replace "LATEST" badge text with "Flash News".
