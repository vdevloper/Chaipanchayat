# Firebase Hosting Intent Redirector & Post Sharing Plan (Revised)

Implement Firebase Hosting smart link redirector infrastructure (`https://gen-lang-client-0700030242.web.app/post/{id}`) that induces the native Android app intent, ensures 100% clickability across WhatsApp, Telegram, and SMS, attaches the featured image, and completely prevents routing users to the WordPress website.

## User Review & Critical Decisions

> [!IMPORTANT]
> **The Firebase Hosting Intent-Inducing Solution**:
> 1. **Why Custom Schemes Fail Alone**: Plain schemes like `chaipanchayat://post/123` cannot be clicked inside WhatsApp, SMS, or Telegram because messaging clients only activate `http://` and `https://` protocols.
> 2. **Why WordPress Links Fail**: Sharing `https://chaipanchayat.com/?p=123` defaults to opening the full WordPress website in Chrome instead of keeping users in the app.
> 3. **The Firebase Hosting Bridge**:
>    - We configure Firebase Hosting for project `gen-lang-client-0700030242` (`public/index.html` + `firebase.json`).
>    - Share link format: `https://gen-lang-client-0700030242.web.app/post/{id}`.
>    - Because it is a secure `https://` address, all social and messaging apps render it as a clickable blue hyperlink with rich Open Graph previews.
>    - When tapped on Android with the app installed, Android's `intent-filter` intercepts the domain immediately without launching Chrome.
>    - If tapped in a mobile browser, Firebase Hosting serves an intent-inducing script (`intent://post/{id}#Intent;scheme=chaipanchayat;package=com.chaipanchayat.app;end` and `location.href = "chaipanchayat://post/{id}"`) which immediately opens the native app, with a fallback button to Google Play.

- **Confirmed Decision 1**: Share messages will use `https://gen-lang-client-0700030242.web.app/post/{id}` as the primary clickable URL, paired with the downloaded featured image via `FileProvider`.
- **Confirmed Decision 2**: Zero WordPress redirects—the Firebase Hosting endpoint induces the native Android intent directly, keeping the experience strictly in-app.

---

## 1. Overview & Core Concept

- **What It Does**: When any article or video post is shared, the app attaches the featured image and generates a formatted message containing the headline and a clickable Firebase Hosting link. Tapping the link opens the Chai Panchayat app directly to that specific post.
- **Target Audience / Persona**: Readers sharing news stories with friends and groups on WhatsApp, Telegram, Facebook, and SMS.
- **Key Value**: Delivers frictionless sharing with attractive image previews and guaranteed in-app opening without visiting the website.

```
┌─────────────────────────────────────────────────────────────┐
│                 Chai Panchayat Story Share                  │
├─────────────────────────────────────────────────────────────┤
│ [Featured Image Attached]                                   │
│                                                             │
│ 🔥 {Post Headline}                                          │
│                                                             │
│ 📲 ऐप में सीधे पढ़ें (Tap to Open in App):                 │
│ https://gen-lang-client-0700030242.web.app/post/{id}        │
│                                                             │
│ (चाय पंचायत - बेबाक, सटीक, निष्पक्ष खबरें)                  │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. User Experience & Visual Design

- **Key User Flows**:
  1. **Share Trigger**: User taps the Share button on any `NewsCard`, `HeroCard`, `ArticleScreen`, or `VideosScreen`.
  2. **Image Preparation**: High-res featured image is retrieved from Coil cache or fetched via OkHttp and placed into the app cache (`shared_images/share_post_{id}.jpg`).
  3. **Intent Dispatch**: `Intent.ACTION_SEND` opens the Android system share sheet with both the image URI and the text containing `https://gen-lang-client-0700030242.web.app/post/{id}`.
  4. **Recipient Tap (In-App Direct)**: Recipient clicks the link on Android. The system matches the `autoVerify` intent filter on `gen-lang-client-0700030242.web.app` and launches `MainActivity` directly into `ArticleScreen(postId)`.
  5. **Recipient Tap (Browser Fallback)**: If opened in a web browser, the Firebase Hosting page immediately executes an Android Intent URI launch (`intent://post/{id}#Intent;scheme=chaipanchayat;package=com.chaipanchayat.app;end`) to induce the app, featuring a clean Hindi/English branded button "ऐप में खोलें (Open in Chai Panchayat App)".

- **Visual Identity & Theme**:
  - Consistent with Chai Panchayat's saffron/maroon and warm cream tones (`#E65100`, `#BF360C`, `#FFF8E1`).
  - Clear Hindi CTA labels: "ऐप में सीधे पढ़ें" and "चाय पंचायत पर पूरी खबर देखें".

---

## 3. Key Product Decisions & Trade-Offs

- **Decision 1: Firebase Hosting as the Link Host**:
  - *Chosen Approach*: Deploy a lightweight intent-inducing web redirector on Firebase Hosting (`public/` directory with `firebase.json` rewrites).
  - *Why*: Messaging apps strictly require standard `https://` protocol to create clickable links. Firebase Hosting is fast, free, Google-backed, and matches the project's Firebase backend.
  - *Alternatives Considered*: Direct custom scheme `chaipanchayat://` was rejected because messaging apps render it as non-clickable plain text; `chaipanchayat.com` was rejected because it routes users to the web.

- **Decision 2: Intent Induction Script on Web Landing**:
  - *Chosen Approach*: The Firebase page runs:
    ```javascript
    const postId = window.location.pathname.split('/').pop() || new URLSearchParams(window.location.search).get('p');
    window.location.href = `intent://post/${postId}#Intent;scheme=chaipanchayat;package=com.chaipanchayat.app;S.browser_fallback_url=market%3A%2F%2Fdetails%3Fid%3Dcom.chaipanchayat.app;end`;
    ```
  - *Why*: Guarantees that even if the Android OS opens the link in Chrome, Chrome immediately prompts to hand off execution to the native Chai Panchayat app.

---

## 4. Technical Architecture & Data Strategy *(Technical Reference)*

### Architecture & Component Diagram

```
┌────────────────────────────────────────────────────────┐
│               Chai Panchayat Android App               │
│                                                        │
│  NewsCard / ArticleScreen / VideosScreen               │
│                    │                                   │
│                    ▼ (User taps Share)                 │
│         DeepLinkManager.shareArticle                   │
│         - Downloads / resolves featured image          │
│         - Writes to FileProvider (shared_images/)      │
│         - Builds text with Firebase link:              │
│           https://gen-lang-client-0700030242.web.app/  │
│           post/{id}                                    │
│         - Dispatches ACTION_SEND chooser               │
└────────────────────┬───────────────────────────────────┘
                     │
                     ▼
┌────────────────────────────────────────────────────────┐
│       Recipient Messaging App (WhatsApp, Telegram)      │
│       - Displays clickable https:// link               │
│       - Displays featured photo                        │
└────────────────────┬───────────────────────────────────┘
                     │ (User taps link)
                     ▼
          ┌───────────────────────────┐
          │ Is App Installed?         │
          └─────┬───────────────┬─────┘
                │ Yes           │ No / Web
                ▼               ▼
┌─────────────────────────┐   ┌───────────────────────────┐
│ Android App Link Inter- │   │ Firebase Hosting          │
│ cept (AndroidManifest)  │   │ (public/index.html)       │
│ - Launches MainActivity │   │ - Runs Android Intent:    │
│ - Navigates directly to │   │   intent://post/{id}      │
│   ArticleScreen(postId) │   │ - Fallback: Play Store    │
└─────────────────────────┘   └───────────────────────────┘
```

### File & Component Changes:
1. **`firebase.json` & `public/index.html`**:
   - Create Firebase Hosting config routing all `/post/**` requests to an intent redirector page with Chai Panchayat branding and immediate `intent://` induction.
2. **`AndroidManifest.xml`**:
   - Add `<intent-filter>` for host `gen-lang-client-0700030242.web.app` and `gen-lang-client-0700030242.firebaseapp.com` under pathPrefix `/post/`.
3. **`DeepLinkManager.kt`**:
   - Update `buildShareMessage` to format the Firebase Hosting link.
   - Update `extractArticleId` to parse the Firebase Hosting domain paths (`/post/{id}` and `/?p={id}`).
   - Ensure image sharing attaches JPEG via `FileProvider` with `FLAG_GRANT_READ_URI_PERMISSION`.
4. **`file_paths.xml`**:
   - Ensure `shared_images` cache path is fully mapped for FileProvider.
