package com.chaipanchayat.app.engine.liquidglass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The two official submodes for Liquid Glass:
 * - DARK: Deep obsidian-charcoal studio canvas with smoked translucent crystal glass & silver specular rims (iOS 26 Dark / Smart Home Dark).
 * - LIGHT: Airy daylight pearl-alabaster canvas with milky-white frosted crystal glass & crisp white specular rims (Monobank / Daylight Glass).
 */
enum class GlassSubMode(
    val key: String,
    val title: String,
    val hindiTitle: String
) {
    DARK(
        key = "dark",
        title = "Dark Glass",
        hindiTitle = "डार्क ग्लास"
    ),
    LIGHT(
        key = "light",
        title = "Light Glass",
        hindiTitle = "लाइट ग्लास"
    );

    companion object {
        fun fromKey(key: String?): GlassSubMode =
            entries.find { it.key == key } ?: DARK
    }
}

enum class GlassTintStyle(
    val key: String,
    val title: String,
    val accentColor: Color,
    val surfaceColor: Color,
    val surfaceSecondaryColor: Color,
    val rimHighlight: Color,
    val glowColor: Color
) {
    CRYSTAL_CYAN(
        key = "crystal_cyan",
        title = "Clear Crystal Glass",
        accentColor = Color(0xFFFF7A28),
        surfaceColor = Color(0x5218191E),
        surfaceSecondaryColor = Color(0x661C1E24),
        rimHighlight = Color(0x78FFFFFF),
        glowColor = Color(0x20FFFFFF)
    ),
    FROSTED_ICE(
        key = "frosted_ice",
        title = "Frosted White",
        accentColor = Color(0xFFF4F4F5),
        surfaceColor = Color(0x521E1E22),
        surfaceSecondaryColor = Color(0x6627272A),
        rimHighlight = Color(0x82FFFFFF),
        glowColor = Color(0x24FFFFFF)
    ),
    ROYAL_SAFFRON(
        key = "royal_saffron",
        title = "Royal Saffron",
        accentColor = Color(0xFFFF6B00),
        surfaceColor = Color(0x52181412),
        surfaceSecondaryColor = Color(0x66221C18),
        rimHighlight = Color(0x70FFE8D6),
        glowColor = Color(0x1AFF6B00)
    ),
    OBSIDIAN_SMOKE(
        key = "obsidian_smoke",
        title = "Obsidian Smoke",
        accentColor = Color(0xFFD4D4D8),
        surfaceColor = Color(0x580E0E10),
        surfaceSecondaryColor = Color(0x6B161618),
        rimHighlight = Color(0x66F4F4F5),
        glowColor = Color(0x18FFFFFF)
    ),
    AURORA_EMERALD(
        key = "aurora_emerald",
        title = "Natural Sage",
        accentColor = Color(0xFF34D399),
        surfaceColor = Color(0x52121614),
        surfaceSecondaryColor = Color(0x6618201C),
        rimHighlight = Color(0x66ECFDF5),
        glowColor = Color(0x1810B981)
    );

    companion object {
        fun fromKey(key: String): GlassTintStyle =
            entries.find { it.key == key } ?: CRYSTAL_CYAN
    }
}

enum class GlassBlurDepth(val key: String, val title: String, val radius: Dp) {
    SUBTLE("subtle", "Subtle (16dp)", 16.dp),
    BALANCED("balanced", "Balanced (28dp)", 28.dp),
    DEEP("deep", "Deep Frost (34dp)", 34.dp),
    ULTRA("ultra", "Ultra Lens (42dp)", 42.dp);

    companion object {
        fun fromKey(key: String): GlassBlurDepth =
            entries.find { it.key == key } ?: DEEP
    }
}

@Immutable
data class LiquidGlassSettings(
    val subMode: GlassSubMode = GlassSubMode.DARK,
    val tintStyle: GlassTintStyle = GlassTintStyle.CRYSTAL_CYAN,
    val blurDepth: GlassBlurDepth = GlassBlurDepth.DEEP,
    val frostOpacity: Float = 0.62f,
    val chromaticRim: Boolean = true,
    val specularShimmer: Boolean = true,
    val touchPhysics: Boolean = true,
    val ambientOrbs: Boolean = true,
    val specularIntensity: Float = 0.55f
) {
    val isLightGlass: Boolean
        get() = subMode == GlassSubMode.LIGHT

    /**
     * Generates a physical top-down directional glass rim.
     */
    fun createRimBrush(): Brush {
        return if (isLightGlass) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x360F172A),
                    Color(0x1A0F172A),
                    Color(0x140F172A),
                    Color(0x280F172A)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0x6BFFFFFF),
                    Color(0x28FFFFFF),
                    Color(0x12FFFFFF),
                    Color(0x22FFFFFF)
                )
            )
        }
    }

    /**
     * Subtle top-to-bottom inner sheen representing light entering the glass pane.
     */
    fun createSpecularBevelBrush(): Brush {
        return if (isLightGlass) {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.42f),
                    Color.White.copy(alpha = 0.12f),
                    Color.Transparent
                )
            )
        } else {
            val alpha = (0.12f * specularIntensity).coerceIn(0.03f, 0.14f)
            Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = alpha),
                    Color.White.copy(alpha = alpha * 0.2f),
                    Color.Transparent
                )
            )
        }
    }
}
