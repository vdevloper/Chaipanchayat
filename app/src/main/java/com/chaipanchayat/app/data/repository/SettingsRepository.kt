package com.chaipanchayat.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class TextSizePreference(val key: String, val displayName: String, val multiplier: Float) {
    SMALL("small", "Small", 0.9f),
    MEDIUM("medium", "Medium", 1.0f),
    LARGE("large", "Large", 1.15f);

    companion object {
        fun fromKey(key: String): TextSizePreference {
            return entries.find { it.key == key } ?: MEDIUM
        }
    }
}

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("chai_settings_prefs", Context.MODE_PRIVATE)

    private val _textSize = MutableStateFlow(
        TextSizePreference.fromKey(prefs.getString(KEY_TEXT_SIZE, TextSizePreference.MEDIUM.key) ?: "medium")
    )
    val textSize: StateFlow<TextSizePreference> = _textSize.asStateFlow()

    fun setTextSize(size: TextSizePreference) {
        prefs.edit().putString(KEY_TEXT_SIZE, size.key).apply()
        _textSize.value = size
    }

    fun cycleTextSize() {
        val next = when (_textSize.value) {
            TextSizePreference.SMALL -> TextSizePreference.MEDIUM
            TextSizePreference.MEDIUM -> TextSizePreference.LARGE
            TextSizePreference.LARGE -> TextSizePreference.SMALL
        }
        setTextSize(next)
    }

    companion object {
        private const val KEY_TEXT_SIZE = "text_size_preference"

        @Volatile
        private var INSTANCE: SettingsRepository? = null

        fun getInstance(context: Context): SettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
