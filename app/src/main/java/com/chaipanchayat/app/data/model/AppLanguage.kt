package com.chaipanchayat.app.data.model

import java.util.Locale

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String,
    val locale: Locale
) {
    HINDI("hi", "हिन्दी", "Hindi", "🇮🇳", Locale("hi", "IN")),
    ENGLISH("en", "English", "English", "🇬🇧", Locale.ENGLISH),
    SPANISH("es", "Español", "Spanish", "🇪🇸", Locale("es", "ES")),
    FRENCH("fr", "Français", "French", "🇫🇷", Locale.FRENCH),
    GERMAN("de", "Deutsch", "German", "🇩🇪", Locale.GERMAN),
    RUSSIAN("ru", "Русский", "Russian", "🇷🇺", Locale("ru", "RU")),
    ARABIC("ar", "العربية", "Arabic", "🇸🇦", Locale("ar", "SA")),
    BENGALI("bn", "বাংলা", "Bengali", "🇮🇳", Locale("bn", "IN")),
    MARATHI("mr", "मराठी", "Marathi", "🇮🇳", Locale("mr", "IN")),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "🇮🇳", Locale("gu", "IN")),
    TAMIL("ta", "தமிழ்", "Tamil", "🇮🇳", Locale("ta", "IN")),
    TELUGU("te", "తెలుగు", "Telugu", "🇮🇳", Locale("te", "IN"));

    companion object {
        fun fromCode(code: String?): AppLanguage {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: HINDI
        }
    }
}
