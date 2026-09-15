package com.chaipanchayat.app.utils

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback

class ChaiHaptics(
    private val hapticFeedback: HapticFeedback,
    private val vibrator: Vibrator?
) {
    /** Light sensory feedback for everyday clicks, chips, tab selection */
    fun click() {
        try {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            }
        } catch (_: Exception) { }
    }

    /** Medium sensory feedback for bookmarks, audio controls, refresh trigger */
    fun medium() {
        try {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(25)
            }
        } catch (_: Exception) { }
    }

    /** Distinct haptic confirmation for bookmark saved, refresh complete */
    fun success() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 15, 60, 25),
                        intArrayOf(0, 120, 0, 200),
                        -1
                    )
                )
            } else {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        } catch (_: Exception) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    /** Heavy haptic for fullscreen toggle, rotate video screen */
    fun heavy() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(45)
            }
            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Exception) { }
    }
}

@Composable
fun rememberChaiHaptics(): ChaiHaptics {
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    return remember(hapticFeedback, context) {
        val vibrator = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
        ChaiHaptics(hapticFeedback, vibrator)
    }
}
