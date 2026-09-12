package com.chaipanchayat.app.engine.liquidglass

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

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
        title = "Crystal Cyan (iOS Liquid Glass)",
        accentColor = Color(0xFF00E5FF),
        surfaceColor = Color(0xD00A1322),
        surfaceSecondaryColor = Color(0xC0101E35),
        rimHighlight = Color(0x9938BDF8),
        glowColor = Color(0x4000E5FF)
    ),
    FROSTED_ICE(
        key = "frosted_ice",
        title = "Frosted Ice (Pure Acrylic Glass)",
        accentColor = Color(0xFFE2E8F0),
        surfaceColor = Color(0xC81E293B),
        surfaceSecondaryColor = Color(0xBB334155),
        rimHighlight = Color(0xAAFFFFFF),
        glowColor = Color(0x28FFFFFF)
    ),
    ROYAL_SAFFRON(
        key = "royal_saffron",
        title = "Royal Saffron (Chai Panchayat Amber)",
        accentColor = Color(0xFFFF5400),
        surfaceColor = Color(0xD41A0E08),
        surfaceSecondaryColor = Color(0xC526150C),
        rimHighlight = Color(0xAAFF7733),
        glowColor = Color(0x40FF5400)
    ),
    OBSIDIAN_SMOKE(
        key = "obsidian_smoke",
        title = "Obsidian Smoke (Deep Stealth)",
        accentColor = Color(0xFFA855F7),
        surfaceColor = Color(0xE005070B),
        surfaceSecondaryColor = Color(0xD20B0F17),
        rimHighlight = Color(0x80C084FC),
        glowColor = Color(0x30A855F7)
    ),
    AURORA_EMERALD(
        key = "aurora_emerald",
        title = "Aurora Emerald (Nordic Glass)",
        accentColor = Color(0xFF10B981),
        surfaceColor = Color(0xD0061914),
        surfaceSecondaryColor = Color(0xC20B271F),
        rimHighlight = Color(0x9934D399),
        glowColor = Color(0x3810B981)
    );

    companion object {
        fun fromKey(key: String): GlassTintStyle =
            entries.find { it.key == key } ?: CRYSTAL_CYAN
    }
}

enum class GlassBlurDepth(val key: String, val title: String, val radius: Dp) {
    SUBTLE("subtle", "Subtle (8dp)", 8.dp),
    BALANCED("balanced", "Balanced (16dp)", 16.dp),
    DEEP("deep", "Deep Frost (24dp)", 24.dp),
    ULTRA("ultra", "Ultra Lens (36dp)", 36.dp);

    companion object {
        fun fromKey(key: String): GlassBlurDepth =
            entries.find { it.key == key } ?: BALANCED
    }
}

@Immutable
data class LiquidGlassSettings(
    val tintStyle: GlassTintStyle = GlassTintStyle.CRYSTAL_CYAN,
    val blurDepth: GlassBlurDepth = GlassBlurDepth.BALANCED,
    val frostOpacity: Float = 0.76f,
    val chromaticRim: Boolean = true,
    val specularShimmer: Boolean = true,
    val touchPhysics: Boolean = true,
    val ambientOrbs: Boolean = true,
    val specularIntensity: Float = 0.70f
) {
    /**
     * Generates a multi-stop iridescent prism border simulating real optical refraction.
     */
    fun createRimBrush(): Brush {
        return if (chromaticRim) {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x99FFFFFF),
                    Color(0x28FFFFFF),
                    tintStyle.rimHighlight,
                    tintStyle.accentColor.copy(alpha = 0.55f),
                    Color(0x30FFFFFF),
                    tintStyle.rimHighlight.copy(alpha = 0.35f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color(0x60FFFFFF),
                    Color(0x18FFFFFF),
                    Color(0x40FFFFFF)
                )
            )
        }
    }

    /**
     * Top-to-bottom specular bevel highlight representing light entering the glass pane.
     */
    fun createSpecularBevelBrush(): Brush {
        val alpha = (0.35f * specularIntensity).coerceIn(0.05f, 0.60f)
        return Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                Color.White.copy(alpha = alpha * 0.3f),
                Color.Transparent
            )
        )
    }
}
