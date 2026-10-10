# Chai Panchayat - Fresh Install Crash Hotfix & Adaptive Display Layouts (v2.2.2 / Build 6)

Emergency hotfix and layout stability update to eliminate fresh-install onboarding launch crashes, provide adaptive gutter margins for curved/waterfall displays, and modernize Android 15/16 edge-to-edge and window resizability compliance for Google Play.

---

### User Review & Critical Decisions

> [!IMPORTANT]
> The following directions were confirmed during interactive clarification and Google Play pre-launch audit analysis:

- **Root Cause of Fresh-Install Crash**: Eager `TextToSpeech` service binding in `OnboardingScreen` (Page 0) without runtime engine availability checks, exception handling, or Android 11+ `<queries>` declarations. Resolved by moving TTS to safe lazy on-demand initialization with complete `try/catch` fallbacks, plus declaring `<queries>` for `android.intent.action.TTS_SERVICE` in the manifest.
- **Curved & Waterfall Display Layout**: Confirmed implementation of adaptive display cutout insets (`WindowInsets.displayCutout`), system bar insets (`WindowInsets.safeDrawing`), and adaptive horizontal gutter margins (16dp to 24dp) to prevent text clipping along curved screen edges.
- **Emergency Release Versioning**: Version Code bumped from `5` to `6`, and Version Name updated to `2.2.2` (formatted for Play Console as `2.2.2 (6)`).
- **Google Play Warnings & Android 15/16 Compatibility**:
  - Migrate away from deprecated `Window.setStatusBarColor`, `Window.setNavigationBarColor`, and legacy cutout short edges flags, relying on modern standard `enableEdgeToEdge()` and Compose inset handling.
  - Remove all implicit and explicit orientation or window size restrictions (`resizeableActivity="true"`, multi-window support enabled) to satisfy Android 16 foldable and tablet requirements.

---

### 1. Overview & Core Concept

- **What It Does**: Ensures that any new user installing Chai Panchayat opens directly into a butter-smooth, crash-free onboarding experience. Content and touch targets remain fully legible and operable on any device form factor—including curved edge displays, waterfall screens, foldables, and tablets.
- **Target Audience / Persona**: Hindi news readers, editorial enthusiasts, and first-time app installers across diverse Android devices ranging from budget phones (without default TTS engines) to flagship waterfall/curved edge devices and foldable displays.
- **Key Value**: Immediate crash elimination for live advertising traffic, zero lost installs, and flawless adherence to Google Play production and pre-launch standards.

---

### 2. User Experience & Visual Design

- **Key User Flows**:
  1. *Fresh Install Cold Launch*: App starts instantly into `OnboardingScreen`. The welcome header, animated steaming Kulhad chai cup, and typography load with zero latency.
  2. *Whimsical Audio Preview (Page 0)*: Speech engine is NOT initialized in the background on startup. When the user taps "सुनें" (Listen), the app lazily checks TTS availability inside a protected coroutine. If available, it plays the warm Hindi audio snippet; if unavailable or disabled on the device, it provides an unobtrusive visual audio card preview without crashing.
  3. *Curved Display Edge Protection*: On screens with curved or waterfall edges, horizontal content gutters dynamically expand (20dp–24dp) using `Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))` so text never falls into the bezel refraction zone.
  4. *Large Screen & Foldable Layout*: Layout expands smoothly with centered reading max-widths (`Modifier.widthIn(max = 680.dp)`), adapting cleanly to unfolded screens without stretching or letterboxing.

- **Visual Identity & Theme**:
  - *Aesthetic Direction*: Warm, authentic editorial Hindi journalism with refined clay/terracotta and brass accents (`ChaiSaffron`, `ChaiAmber`, `ChaiCrimson`).
  - *Color Palette & Tokens*:
    - Primary Accent: Terracotta Warm Saffron (`#E05326`)
    - Secondary Accent: Masala Amber (`#D97706`)
    - Surface & Card Backgrounds: Adaptive M3 elevated surfaces with subtle borders (`ChaiTheme.extended.surfaceSecondary`, `ChaiTheme.extended.border`)
  - *Typography Hierarchy*: `NotoSerifFamily` ExtraBold for headlines; `InterFamily` for metadata, badges, and body copy.
  - *Component Styling*: Rounded corners (16dp–24dp), soft shadows, and clean tactile buttons with haptic feedback.

---

### 3. Key Product Decisions & Trade-Offs

- **Decision 1: Lazy vs. Eager TextToSpeech Initialization**
  - *Chosen Approach*: Defer `TextToSpeech` service binding until the user explicitly taps "सुनें" (Listen). Wrap initialization and speech requests in robust exception handlers with a fallback state if no TTS engine is installed.
  - *Why*: Eliminates the #1 fresh-install crash vector while reducing app startup memory and CPU overhead.
  - *Alternatives Considered*: Retaining eager initialization with try/catch was rejected because binding to a system service on frame 1 slows initial render and causes avoidable IPC failures on constrained hardware.

- **Decision 2: Adaptive Insets & Display Cutouts for Curved Screens**
  - *Chosen Approach*: Replace hardcoded status/navigation bar paddings with `WindowInsets.safeDrawing` and `WindowInsets.displayCutout`, paired with consistent adaptive horizontal margin gutters across onboarding, home feed, and article reader screens.
  - *Why*: Waterfall and curved screens have steep edge angles where text within 8–12dp of the screen edge becomes distorted or difficult to read.
  - *Alternatives Considered*: Fixed 32dp padding on all devices was rejected because it wastes valuable screen real estate on flat standard screens.

- **Decision 3: Android 15 & 16 Deprecation Cleanup**
  - *Chosen Approach*: Strictly rely on Compose `enableEdgeToEdge()` in `MainActivity` without invoking legacy window flags. Ensure `AndroidManifest.xml` explicitly defines `android:resizeableActivity="true"` and avoids restricted orientation locks.
  - *Why*: Prevents pre-launch warnings from escalating into release blocks on Android 15/16 devices.

---

### 4. Technical Architecture & Data Strategy

#### Architecture & Component Hierarchy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        MainActivity (v2.2.2)                           │
│  enableEdgeToEdge() • resizeableActivity="true" • Safe Inset Root     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      Navigation & Launch Router                        │
│             Check: settingsRepo.isOnboardingCompleted                  │
└───────────────────┬────────────────────────────────┬───────────────────┘
                    │ (false - Fresh Install)         │ (true - Returning)
                    ▼                                ▼
┌───────────────────────────────────────┐  ┌─────────────────────────────┐
│           OnboardingScreen            │  │      Main App Feed          │
│ ┌───────────────────────────────────┐ │  │  (Home / Categories /       │
│ │ Curved Display Gutter Wrapper     │ │  │   Article Detail Screens)   │
│ │ safeDrawing + displayCutout       │ │  │  Adaptive insets & gutters  │
│ └─────────────────┬─────────────────┘ │  └─────────────────────────────┘
│                   ▼                   │
│ ┌───────────────────────────────────┐ │
│ │ Page 0: Kulhad Chai & Audio Card  │ │
│ │ • Lazy Safe TTS on demand         │ │
│ │ • try-catch error boundary        │ │
│ └───────────────────────────────────┘ │
│ ┌───────────────────────────────────┐ │
│ │ Page 1: Live Font & Speed Test    │ │
│ └───────────────────────────────────┘ │
│ ┌───────────────────────────────────┐ │
│ │ Page 2: Morning Digest Opt-In     │ │
│ └───────────────────────────────────┘ │
│ ┌───────────────────────────────────┐ │
│ │ Page 3: Offline Quick-Pack Caching│ │
│ └───────────────────────────────────┘ │
│ ┌───────────────────────────────────┐ │
│ │ Page 4: Interactive Topic Curator │ │
│ └───────────────────────────────────┘ │
└───────────────────────────────────────┘
```

#### State & Implementation Mapping

1. **`OnboardingScreen.kt`**:
   - Refactor `WhimsicalAudioGreetingPreview()` to initialize `TextToSpeech` only on user action via a dedicated safe coroutine/helper.
   - Guard against missing TTS engines (`TextToSpeech.ERROR`, `LANG_MISSING_DATA`, or missing OEM service) with graceful inline state updates and toast fallbacks.
   - Wrap the main onboarding container with `Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))` and adaptive horizontal padding (`Modifier.padding(horizontal = 20.dp)`).
2. **`AndroidManifest.xml`**:
   - Add `<queries>` block with `<intent><action android:name="android.intent.action.TTS_SERVICE" /></intent></queries>` to satisfy Android 11+ package visibility rules.
   - Add `android:resizeableActivity="true"` to `<application>` and `<activity>`.
3. **`app/build.gradle.kts`**:
   - Increment `versionCode` to `6`.
   - Increment `versionName` to `"2.2.2"`.
4. **Layout Verification across Main Feed & Article Screens**:
   - Verify that `ArticleDetailScreen` and main feed cards use safe drawing insets and comfortable margins for curved screen edges.
