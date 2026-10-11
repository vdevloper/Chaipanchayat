# Unified Global Language & Content Translation Plan (Revised)

Unify homepage and article translation into a single, seamless global experience where setting a language from the homepage automatically translates all feed headlines and articles. Crucially, all language switchers, buttons, and toggles are fully localized and universally recognizable so readers who do not know Hindi can effortlessly navigate, switch between English/Spanish/etc., and toggle back and forth.

## User Feedback Addressed: Non-Hindi Speaker Accessibility

> [!IMPORTANT]
> **Issue Identified**: If language controls or toggle buttons stay in Hindi after translating, a user who does not speak Hindi will get trapped or confused when trying to switch from English to Spanish, French, or another language.
>
> **The Solution**:
> 1. **Universal Globe & Flag Indicator (`🌐 🇬🇧 English ▾`)**:
>    - The language switcher button on the Homepage and Article reader always displays the universal international globe icon (`🌐`), the active language's flag, and the language name in its own script/English (e.g., `🌐 🇬🇧 English ▾` or `🌐 🇪🇸 Español ▾`).
>    - It is instantly recognizable to anyone from any country, regardless of language proficiency.
> 2. **Fully Localized Quick-Toggles**:
>    - The quick toggle to view original Hindi is localized to the active language:
>      - In English: `[ 🇮🇳 View Original (Hindi) ]`
>      - In Spanish: `[ 🇮🇳 Ver original (Hindi) ]`
>      - In French: `[ 🇮🇳 Voir l'original (Hindi) ]`
>    - When switched to original Hindi, the restore button clearly says:
>      - `[ 🌐 Show Translation (English) ]`
> 3. **Dual-Script Language Picker (`LanguagePickerSheet`)**:
>    - Every language in the bottom sheet displays both its native name and English name:
>      - `🇮🇳 हिन्दी • Hindi`
>      - `🇬🇧 English`
>      - `🇪🇸 Español • Spanish`
>      - `🇫🇷 Français • French`
>      - `🇩🇪 Deutsch • German`
>      - `🇷🇺 Русский • Russian`
>      - `🇸🇦 العربية • Arabic`
>      - `🇮🇳 বাংলা • Bengali`
>      - `🇮🇳 मराठी • Marathi`
>      - `🇮🇳 ગુજરાતી • Gujarati`
>    - Header and helper text adapt dynamically to the currently selected language using `ChaiLocalization`.
> 4. **Single Source of Truth**:
>    - Changing the language anywhere (Homepage header, Article reader, or Settings) updates `SettingsRepository.appLanguage`, which synchronously updates all feed cards, article reader, audio reader, and UI strings.
> 5. **Version Bump**:
>    - Incrementing `versionCode = 9` and `versionName = "2.2.4"` in `app/build.gradle.kts` and `SettingsScreen.kt`.

---

## 1. UX & Visual Architecture

```
┌──────────────────────────────────────────────────────────────┐
│  Homepage Header                                             │
│  [ Chai Panchayat Logo ]          [ 🌐 🇬🇧 English ▾ ] [ 🔍 ] │
└──────────────────────────────┬───────────────────────────────┘
                               │ Tapping opens Language Picker
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Language Picker Bottom Sheet (Fully Accessible)             │
│  "Select Language / भाषा चुनें"                               │
│  • 🇬🇧 English                             [ Selected ✓ ]     │
│  • 🇪🇸 Español • Spanish                                      │
│  • 🇮🇳 हिन्दी • Hindi                                          │
│  • 🇫🇷 Français • French                                       │
│  • 🇩🇪 Deutsch • German                                       │
└──────────────────────────────┬───────────────────────────────┘
                               │ Switching to Español
                               ▼
┌──────────────────────────────────────────────────────────────┐
│  Homepage Header Updates Automatically:                      │
│  [ 🌐 🇪🇸 Español ▾ ]                                         │
│                                                              │
│  Feed Banner:                                                │
│  [ 🌐 Traducido al español ]   [ 🇮🇳 Ver original (Hindi) ]   │
│                                                              │
│  Feed Headlines translated dynamically into Spanish          │
└──────────────────────────────────────────────────────────────┘
```

---

## 2. Technical Implementation Details

### A. Localized Strings (`ChaiLocalization.kt`)
Add localized phrases across all supported languages for the translation actions:
- `viewOriginalHindi`:
  - English: `"View Original (Hindi)"`
  - Spanish: `"Ver original (Hindi)"`
  - French: `"Voir l'original (Hindi)"`
  - German: `"Original anzeigen (Hindi)"`
  - Hindi: `"मूल हिंदी देखें"`
  - Gujarati: `"મૂળ હિન્દી જુઓ"`
  - Marathi: `"मूळ हिंदी पहा"`
  - Bengali: `"মূল হিন্দি দেখুন"`
- `showTranslation`:
  - English: `"Show Translation"`
  - Spanish: `"Mostrar traducción"`
  - French: `"Afficher la traduction"`
  - German: `"Übersetzung anzeigen"`
  - Hindi: `"अनुवाद दिखाएं"`
- `translatedToLanguage(langName)`:
  - English: `"Translated into %s"`
  - Spanish: `"Traducido al %s"`
  - French: `"Traduit en %s"`
  - Hindi: `"%s में अनुवादित"`

### B. High-Performance Feed Translation Engine (`TranslationService.kt`)
- `translateBatchTitles(posts: List<WPPost>, targetLang: AppLanguage): Map<Long, String>`
- Uses in-memory LRU cache (`titleCache`) keyed by `"${postId}_${targetLang.code}"`.
- Translates feed headlines concurrently using Kotlin coroutines without freezing scrolling or user input.

### C. Homepage Feed Integration (`HomeScreen.kt`)
- Prominent top bar button: `[ 🌐 ${currentAppLang.flag} ${currentAppLang.englishName} ▾ ]`
- When `currentAppLang != AppLanguage.HINDI`:
  - A clean translation bar appears above the feed:
    - `"🌐 " + localizedStrings.translatedToLanguage(currentAppLang.nativeName) + " • "`
    - Action button: `[ 🇮🇳 ${localizedStrings.viewOriginalHindi} ]`
  - Tapping this toggles `showOriginalHindi` in `SettingsRepository`.
  - When in original mode, the button toggles to `[ 🌐 ${localizedStrings.showTranslation} ]`.
- Passes the active title (`translated` or `original`) into `HeroCard`, `NewsCard`, `BreakingNewsTicker`, and `TodayChaiDigest`.

### D. Article Screen Integration (`ArticleScreen.kt`)
- When opened:
  - If `currentAppLang != AppLanguage.HINDI` and `!showOriginalHindi`, **automatically loads and displays the translated article**.
  - No need to hunt for a separate translation button.
- Header Action Pill:
  - When viewing translated: `[ 🇮🇳 ${localizedStrings.viewOriginalHindi} ]`
  - When viewing original: `[ 🌐 ${localizedStrings.showTranslation} ]`
  - Language picker chip: `[ 🌐 ${currentAppLang.flag} ${currentAppLang.englishName} ▾ ]` allows changing the target language directly from the article reader, syncing globally back to the home feed.
- Audio Reader (`ChaiAudioReader`):
  - Uses the TTS voice matching the active language (English voice when reading English, Hindi voice when reading Hindi).

### E. Other Feed Screens (`CategoryFeedScreen.kt`, `SearchScreen.kt`, `SavedScreen.kt`)
- Observe `SettingsRepository.appLanguage` and `showOriginalHindi` so all headlines across the app stay in sync.

### F. Version Bump
- Increment `versionCode = 9` and `versionName = "2.2.4"` in `app/build.gradle.kts` and `SettingsScreen.kt`.

---

## 3. Verification Checklist

- [ ] Header button displays `🌐 🇬🇧 English ▾` when English is active, and `🌐 🇪🇸 Español ▾` when Spanish is active.
- [ ] A non-Hindi user can tap the globe/flag button at any time to open the language picker.
- [ ] Language picker displays English names alongside native names (`Español • Spanish`, `हिन्दी • Hindi`).
- [ ] On English mode, the toggle button says `View Original (Hindi)` in English (never untranslated Hindi).
- [ ] Tapping `View Original (Hindi)` toggles all feed headlines to Hindi; button updates to `Show Translation (English)`.
- [ ] Opening any post opens directly in the selected language.
- [ ] Article screen top controls show `View Original (Hindi)` and allow switching to Spanish/French/etc. seamlessly.
- [ ] Version code bumped to 9 and version name to 2.2.4.
- [ ] `compile_applet` runs with 0 errors.
