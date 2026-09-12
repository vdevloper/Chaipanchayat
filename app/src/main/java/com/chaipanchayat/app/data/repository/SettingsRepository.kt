package com.chaipanchayat.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.chaipanchayat.app.engine.liquidglass.GlassBlurDepth
import com.chaipanchayat.app.engine.liquidglass.GlassTintStyle
import com.chaipanchayat.app.engine.liquidglass.LiquidGlassSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

enum class ThemeMode(val key: String, val title: String, val subtitle: String) {
    SYSTEM("system", "System Default", "Follows device appearance"),
    LIGHT("light", "Editorial Light", "Warm newsprint & ivory aesthetic"),
    DARK("dark", "Obsidian Dark", "Deep contrast night mode"),
    LIQUID_GLASS("liquid_glass", "Liquid Glass", "Translucent frosted glass with specular glow");

    companion object {
        fun fromKey(key: String): ThemeMode {
            return entries.find { it.key == key } ?: DARK
        }
    }
}

enum class TextSizePreference(val key: String, val displayName: String, val multiplier: Float) {
    SMALL("small", "Compact", 0.9f),
    MEDIUM("medium", "Standard", 1.0f),
    LARGE("large", "Spacious", 1.18f),
    EXTRA_LARGE("xlarge", "Extra Large", 1.32f);

    companion object {
        fun fromKey(key: String): TextSizePreference {
            return entries.find { it.key == key } ?: MEDIUM
        }
    }
}

enum class ReadingFontPreference(val key: String, val title: String) {
    SERIF("serif", "Editorial Serif (Noto)"),
    SANS("sans", "Modern Sans (Inter)");

    companion object {
        fun fromKey(key: String): ReadingFontPreference {
            return entries.find { it.key == key } ?: SERIF
        }
    }
}

enum class SpeechSpeedPreference(val speed: Float, val label: String) {
    SPEED_0_8(0.85f, "0.8x"),
    SPEED_1_0(1.0f, "1.0x"),
    SPEED_1_25(1.25f, "1.25x"),
    SPEED_1_5(1.5f, "1.5x");

    companion object {
        fun fromSpeed(speed: Float): SpeechSpeedPreference {
            return entries.minByOrNull { kotlin.math.abs(it.speed - speed) } ?: SPEED_1_0
        }
    }
}

class SettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("chai_settings_prefs_v2", Context.MODE_PRIVATE)

    // Theme Mode
    private val _themeMode = MutableStateFlow(
        ThemeMode.fromKey(prefs.getString(KEY_THEME_MODE, ThemeMode.DARK.key) ?: ThemeMode.DARK.key)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    // Liquid Glass Advanced Engine Settings
    private val _liquidGlassSettings = MutableStateFlow(
        LiquidGlassSettings(
            tintStyle = GlassTintStyle.fromKey(prefs.getString(KEY_GLASS_TINT, GlassTintStyle.CRYSTAL_CYAN.key) ?: GlassTintStyle.CRYSTAL_CYAN.key),
            blurDepth = GlassBlurDepth.fromKey(prefs.getString(KEY_GLASS_BLUR, GlassBlurDepth.BALANCED.key) ?: GlassBlurDepth.BALANCED.key),
            frostOpacity = prefs.getFloat(KEY_GLASS_FROST_OPACITY, 0.76f),
            chromaticRim = prefs.getBoolean(KEY_GLASS_CHROMATIC_RIM, true),
            specularShimmer = prefs.getBoolean(KEY_GLASS_SHIMMER, true),
            touchPhysics = prefs.getBoolean(KEY_GLASS_TOUCH_PHYSICS, true),
            ambientOrbs = prefs.getBoolean(KEY_GLASS_AMBIENT_ORBS, true),
            specularIntensity = prefs.getFloat(KEY_GLASS_SPECULAR_INTENSITY, 0.70f)
        )
    )
    val liquidGlassSettings: StateFlow<LiquidGlassSettings> = _liquidGlassSettings.asStateFlow()

    // Text Size
    private val _textSize = MutableStateFlow(
        TextSizePreference.fromKey(prefs.getString(KEY_TEXT_SIZE, TextSizePreference.MEDIUM.key) ?: "medium")
    )
    val textSize: StateFlow<TextSizePreference> = _textSize.asStateFlow()

    // Reading Font
    private val _readingFont = MutableStateFlow(
        ReadingFontPreference.fromKey(prefs.getString(KEY_READING_FONT, ReadingFontPreference.SERIF.key) ?: "serif")
    )
    val readingFont: StateFlow<ReadingFontPreference> = _readingFont.asStateFlow()

    // Speech Speed
    private val _speechSpeed = MutableStateFlow(
        prefs.getFloat(KEY_SPEECH_SPEED, 1.0f)
    )
    val speechSpeed: StateFlow<Float> = _speechSpeed.asStateFlow()

    // Push Notifications (API Pinging Worker)
    private val _pushNotifications = MutableStateFlow(
        prefs.getBoolean(KEY_PUSH_NOTIFICATIONS, true)
    )
    val pushNotifications: StateFlow<Boolean> = _pushNotifications.asStateFlow()

    // Breaking Alerts
    private val _breakingAlerts = MutableStateFlow(
        prefs.getBoolean(KEY_BREAKING_ALERTS, true)
    )
    val breakingAlerts: StateFlow<Boolean> = _breakingAlerts.asStateFlow()

    // Daily Digest
    private val _dailyDigest = MutableStateFlow(
        prefs.getBoolean(KEY_DAILY_DIGEST, true)
    )
    val dailyDigest: StateFlow<Boolean> = _dailyDigest.asStateFlow()

    // Data Saver
    private val _dataSaver = MutableStateFlow(
        prefs.getBoolean(KEY_DATA_SAVER, false)
    )
    val dataSaver: StateFlow<Boolean> = _dataSaver.asStateFlow()

    // Haptics
    private val _haptics = MutableStateFlow(
        prefs.getBoolean(KEY_HAPTICS, true)
    )
    val haptics: StateFlow<Boolean> = _haptics.asStateFlow()

    // General Settings (Spec Section 14)
    private val _autoPlayVideos = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_PLAY_VIDEOS, false)
    )
    val autoPlayVideos: StateFlow<Boolean> = _autoPlayVideos.asStateFlow()

    private val _saveArticles = MutableStateFlow(
        prefs.getBoolean(KEY_SAVE_ARTICLES, true)
    )
    val saveArticles: StateFlow<Boolean> = _saveArticles.asStateFlow()

    private val _language = MutableStateFlow(
        prefs.getString(KEY_LANGUAGE, "हिन्दी") ?: "हिन्दी"
    )
    val language: StateFlow<String> = _language.asStateFlow()

    // Cache Size string
    private val _cacheSizeFormatted = MutableStateFlow(getCacheSizeFormatted())
    val cacheSizeFormatted: StateFlow<String> = _cacheSizeFormatted.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.key).apply()
        _themeMode.value = mode
    }

    fun updateLiquidGlassSettings(transform: (LiquidGlassSettings) -> LiquidGlassSettings) {
        val updated = transform(_liquidGlassSettings.value)
        _liquidGlassSettings.value = updated
        prefs.edit()
            .putString(KEY_GLASS_TINT, updated.tintStyle.key)
            .putString(KEY_GLASS_BLUR, updated.blurDepth.key)
            .putFloat(KEY_GLASS_FROST_OPACITY, updated.frostOpacity)
            .putBoolean(KEY_GLASS_CHROMATIC_RIM, updated.chromaticRim)
            .putBoolean(KEY_GLASS_SHIMMER, updated.specularShimmer)
            .putBoolean(KEY_GLASS_TOUCH_PHYSICS, updated.touchPhysics)
            .putBoolean(KEY_GLASS_AMBIENT_ORBS, updated.ambientOrbs)
            .putFloat(KEY_GLASS_SPECULAR_INTENSITY, updated.specularIntensity)
            .apply()
    }

    fun setTextSize(size: TextSizePreference) {
        prefs.edit().putString(KEY_TEXT_SIZE, size.key).apply()
        _textSize.value = size
    }

    fun cycleTextSize() {
        val next = when (_textSize.value) {
            TextSizePreference.SMALL -> TextSizePreference.MEDIUM
            TextSizePreference.MEDIUM -> TextSizePreference.LARGE
            TextSizePreference.LARGE -> TextSizePreference.EXTRA_LARGE
            TextSizePreference.EXTRA_LARGE -> TextSizePreference.SMALL
        }
        setTextSize(next)
    }

    fun setReadingFont(font: ReadingFontPreference) {
        prefs.edit().putString(KEY_READING_FONT, font.key).apply()
        _readingFont.value = font
    }

    fun setSpeechSpeed(speed: Float) {
        prefs.edit().putFloat(KEY_SPEECH_SPEED, speed).apply()
        _speechSpeed.value = speed
    }

    fun setPushNotifications(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_PUSH_NOTIFICATIONS, enabled).apply()
        _pushNotifications.value = enabled
        if (enabled) {
            com.chaipanchayat.app.worker.ChaiNewsNotificationWorker.schedulePeriodic(context)
        } else {
            com.chaipanchayat.app.worker.ChaiNewsNotificationWorker.cancelPeriodic(context)
        }
    }

    fun getLastSeenPostId(): Long {
        return prefs.getLong(KEY_LAST_SEEN_POST_ID, 0L)
    }

    fun setLastSeenPostId(id: Long) {
        prefs.edit().putLong(KEY_LAST_SEEN_POST_ID, id).apply()
    }

    fun setBreakingAlerts(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BREAKING_ALERTS, enabled).apply()
        _breakingAlerts.value = enabled
    }

    fun setDailyDigest(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_DIGEST, enabled).apply()
        _dailyDigest.value = enabled
    }

    fun setDataSaver(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DATA_SAVER, enabled).apply()
        _dataSaver.value = enabled
    }

    fun setHaptics(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS, enabled).apply()
        _haptics.value = enabled
    }

    fun setAutoPlayVideos(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_PLAY_VIDEOS, enabled).apply()
        _autoPlayVideos.value = enabled
    }

    fun setSaveArticles(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SAVE_ARTICLES, enabled).apply()
        _saveArticles.value = enabled
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString(KEY_LANGUAGE, lang).apply()
        _language.value = lang
    }

    fun refreshCacheSize() {
        _cacheSizeFormatted.value = getCacheSizeFormatted()
    }

    suspend fun clearApplicationCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            val cacheDir = context.cacheDir
            deleteDirContents(cacheDir)
            val codeCacheDir = context.codeCacheDir
            deleteDirContents(codeCacheDir)
            refreshCacheSize()
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun deleteDirContents(dir: File?): Boolean {
        if (dir == null || !dir.exists()) return true
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) {
                deleteDirContents(file)
            }
            file.delete()
        }
        return true
    }

    private fun getCacheSizeFormatted(): String {
        var totalBytes: Long = 0
        try {
            val cacheDir = context.cacheDir
            if (cacheDir != null && cacheDir.exists()) {
                totalBytes += getDirSize(cacheDir)
            }
        } catch (_: Exception) {}

        if (totalBytes <= 0) return "0.4 MB"
        val mb = totalBytes.toDouble() / (1024.0 * 1024.0)
        return String.format(java.util.Locale.US, "%.1f MB", mb)
    }

    private fun getDirSize(dir: File): Long {
        var size: Long = 0
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getDirSize(file) else file.length()
        }
        return size
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode_v2"
        private const val KEY_TEXT_SIZE = "text_size_preference"
        private const val KEY_READING_FONT = "reading_font_preference"
        private const val KEY_SPEECH_SPEED = "speech_speed_preference"
        private const val KEY_PUSH_NOTIFICATIONS = "push_notifications_pref"
        private const val KEY_LAST_SEEN_POST_ID = "last_seen_post_id_pref"
        private const val KEY_BREAKING_ALERTS = "breaking_alerts_pref"
        private const val KEY_DAILY_DIGEST = "daily_digest_pref"
        private const val KEY_DATA_SAVER = "data_saver_pref"
        private const val KEY_HAPTICS = "haptics_pref"
        private const val KEY_AUTO_PLAY_VIDEOS = "auto_play_videos_pref"
        private const val KEY_SAVE_ARTICLES = "save_articles_pref"
        private const val KEY_LANGUAGE = "language_pref"

        private const val KEY_GLASS_TINT = "glass_tint_flavor"
        private const val KEY_GLASS_BLUR = "glass_blur_depth"
        private const val KEY_GLASS_FROST_OPACITY = "glass_frost_opacity"
        private const val KEY_GLASS_CHROMATIC_RIM = "glass_chromatic_rim"
        private const val KEY_GLASS_SHIMMER = "glass_specular_shimmer"
        private const val KEY_GLASS_TOUCH_PHYSICS = "glass_touch_physics"
        private const val KEY_GLASS_AMBIENT_ORBS = "glass_ambient_orbs"
        private const val KEY_GLASS_SPECULAR_INTENSITY = "glass_specular_intensity"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
