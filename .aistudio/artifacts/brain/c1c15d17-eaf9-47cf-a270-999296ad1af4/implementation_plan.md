# Direct In-App Post Sharing & Deferred Deep-Linking Plan

Ensure shared posts always open directly inside the Chai Panchayat app (never on the external website), with automatic Google Play Store redirection and deferred deep-linking for recipients who do not yet have the app installed.

## User Review & Critical Decisions

> [!IMPORTANT]
> The external website permalink is removed from the primary share target. Instead, shared posts will use dedicated in-app deep links (`chaipanchayat://post/{id}` and Android Intent URIs) paired with Google Play install parameters so that:
> 1. If recipient has the app: Tapping opens the post directly inside Chai Panchayat (0% website).
> 2. If recipient does NOT have the app: The link directs them to download Chai Panchayat from Google Play, and once downloaded/opened, the app automatically navigates straight to the shared post.

- **Direct In-App Deep Link**:
  - Registered Custom URI Scheme: `chaipanchayat://post/{id}`
  - Android Intent URI: `intent://post/{id}#Intent;scheme=chaipanchayat;package=com.chaipanchayat.app;S.browser_fallback_url=https%3A%2F%2Fplay.google.com%2Fstore%2Fapps%2Fdetails%3Fid%3Dcom.chaipanchayat.app;end`
- **Deferred Deep Linking (Open Post After Install)**:
  - When the app is launched for the first time, it checks the install referrer / launch intent / clipboard for a pending `post_id`.
  - If a pending post ID is detected, the app immediately transitions to `ArticleScreen` for that post rather than staying on the home screen.
- **Dedicated Share Card Text**:
  - Eliminates all links to the WordPress website.
  - Generates clear in-app call-to-action text:
    - Hindi headline & story summary
    - In-app launch link
    - 1-tap Google Play download link

---

## 1. Overview & Core Concept

- **What It Does**: Replaces website link sharing with direct app routing. Recipient devices with Chai Panchayat installed open the article directly inside the app. For recipient devices without the app, it directs them to Google Play to download Chai Panchayat and then automatically opens the shared article upon launch.
- **Key Value**: 100% app-first viral loop; stops sending users away to a mobile web browser and keeps all reading traffic inside the native Liquid Glass app experience.

---

## 2. User Experience & Flow Diagrams

### Scenario A: Recipient Has App Installed

```
[Sender Shares Post] ──> [WhatsApp/Telegram/SMS] ──> [Recipient taps link]
                                                              │
                                                              ▼
                                                 [Chai Panchayat App Launches]
                                                              │
                                                              ▼
                                                 [ArticleScreen Displays Post]
```

### Scenario B: Recipient Does NOT Have App Installed

```
[Recipient taps link] ──> [Directs to Google Play Store]
                                       │
                                       ▼
                           [Installs & Opens App]
                                       │
                                       ▼
                           [App detects shared Post ID]
                                       │
                                       ▼
                           [Directly opens ArticleScreen]
```

- **Back Navigation**: In both scenarios, pressing Back from the opened article brings the user straight to `HomeScreen`, letting them discover more stories and browse the full app.

---

## 3. Key Technical Decisions & Strategy

- **Decision 1: Elimination of Website Links in Sharing**
  - *Chosen Approach*: Share text will specifically use the `chaipanchayat://post/{id}` scheme and Google Play Store link with `&referrer=post_id%3D{id}`.
  - *Why*: Directly addresses your requirement that the link should never send users to browse the website, but strictly keep them in the app.
- **Decision 2: Deferred Deep Linking via Referral & Intent**
  - *Chosen Approach*: Implement a `DeepLinkManager` that intercepts:
    1. Incoming `Intent.ACTION_VIEW` with scheme `chaipanchayat://post/{id}`.
    2. Google Play Install Referrer broadcast / parameter (`post_id=...`).
    3. Clipboard check on fresh install: If the user copied a shared Chai Panchayat post link, the app detects it on first launch and prompts or auto-opens the article.
  - *Why*: Ensures a seamless bridge between downloading the app and immediately viewing the exact article the user wanted to see.
- **Decision 3: Zero Impact on Existing Fixes**
  - *Chosen Approach*: Keep all existing database entities, Room ProGuard rules, and WorkManager configurations untouched.

---

## 4. Technical Architecture & File Changes

```
┌────────────────────────────────────────────────────────┐
│                   AndroidManifest.xml                  │
│  <intent-filter>                                       │
│    <action android:name="android.intent.action.VIEW"/> │
│    <category android:name="android.intent.category.DEFAULT"/>
│    <category android:name="android.intent.category.BROWSABLE"/>
│    <data android:scheme="chaipanchayat" host="post" /> │
│  </intent-filter>                                      │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                   DeepLinkManager.kt                   │
│  - Parses chaipanchayat://post/{id}                    │
│  - Checks Install Referrer & Clipboard for post_id     │
│  - Exposes StateFlow<Long?> for active target post     │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│                     MainActivity                       │
│  - Forwards intent to DeepLinkManager                  │
│  - Handles onCreate and onNewIntent                    │
│  - Injects target post ID into MainAppNavigation       │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│               ArticleScreen & VideosScreen             │
│  - In-app share button generating app & Play Store link│
│  - No website permalinks included                      │
└────────────────────────────────────────────────────────┘
```

### Planned File Edits:
1. **`AndroidManifest.xml`**:
   - Register the `chaipanchayat://post` intent-filter for `MainActivity`.
2. **`DeepLinkManager.kt`** (new utility):
   - Parse custom URI schemes, intent extras, and pending install referrer targets.
3. **`MainActivity.kt`**:
   - Integrate `DeepLinkManager` into `onCreate` and `onNewIntent`.
4. **`MainNavigation.kt`**:
   - Listen for deep-linked article targets and navigate directly to `ArticleScreen`.
5. **`ArticleScreen.kt` & `VideosScreen.kt`**:
   - Update share text format to use the app link and Google Play Store install URL.
