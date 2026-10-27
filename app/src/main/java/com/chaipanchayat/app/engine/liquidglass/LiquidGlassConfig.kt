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
        title = "Liquid Glass (Apple iOS Inspired)",
        accentColor = Color(0xFFFF5A00), // Chai Saffron as primary brand accent
        surfaceColor = Color(0x66182236), // Balanced translucency (40% opacity)
        surfaceSecondaryColor = Color(0x75202E46), // 46% opacity
        rimHighlight = Color(0x73FFFFFF), // Clean specular white highlight rim
        glowColor = Color(0x20FFFFFF) // Soft neutral depth glow
    ),
    FROSTED_ICE(
        key = "frosted_ice",
        title = "Frosted Ice (Pure Acrylic Glass)",
        accentColor = Color(0xFFF1F5F9),
        surfaceColor = Color(0x601E293B),
        surfaceSecondaryColor = Color(0x70334155),
        rimHighlight = Color(0x80FFFFFF),
        glowColor = Color(0x1AFFFFFF)
    ),
    ROYAL_SAFFRON(
        key = "royal_saffron",
        title = "Royal Saffron (Amber Glass)",
        accentColor = Color(0xFFFF5400),
        surfaceColor = Color(0x662A1408),
        surfaceSecondaryColor = Color(0x753B1D0C),
        rimHighlight = Color(0x80FFB380),
        glowColor = Color(0x26FF5400)
    ),
    OBSIDIAN_SMOKE(
        key = "obsidian_smoke",
        title = "Obsidian Smoke (Smoked Glass)",
        accentColor = Color(0xFFC084FC),
        surfaceColor = Color(0x700C111C),
        surfaceSecondaryColor = Color(0x80151D2E),
        rimHighlight = Color(0x60E2E8F0),
        glowColor = Color(0x20000000)
    ),
    AURORA_EMERALD(
        key = "aurora_emerald",
        title = "Aurora Emerald (Nordic Glass)",
        accentColor = Color(0xFF34D399),
        surfaceColor = Color(0x60061914),
        surfaceSecondaryColor = Color(0x700B271F),
        rimHighlight = Color(0x70A7F3D0),
        glowColor = Color(0x2010B981)
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
    val frostOpacity: Float = 0.52f, // Balanced glass translucency
    val chromaticRim: Boolean = true,
    val specularShimmer: Boolean = true,
    val touchPhysics: Boolean = true,
    val ambientOrbs: Boolean = true,
    val specularIntensity: Float = 0.65f
) {
    /**
     * Generates a physical top-down directional glass rim.
     * Highlights the top and upper-left corner with white specular light,
     * fading delicately towards the bottom to give authentic physical thickness.
     */
    fun createRimBrush(): Brush {
        return Brush.linearGradient(
            colors = listOf(
                Color(0x99FFFFFF), // Top specular reflection
                Color(0x40FFFFFF), // Mid transmission
                Color(0x1AFFFFFF), // Low edge
                Color(0x0DFFFFFF)  // Base shadow transition
            ),
            start = androidx.compose.ui.geometry.Offset(0f, 0f),
            end = androidx.compose.ui.geometry.Offset(0f, Float.POSITIVE_INFINITY)
        )
    }

    /**
     * Top-to-bottom specular bevel highlight representing light entering the glass pane.
     */
    fun createSpecularBevelBrush(): Brush {
        val alpha = (0.28f * specularIntensity).coerceIn(0.04f, 0.45f)
        return Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),
                Color.White.copy(alpha = alpha * 0.25f),
                Color.Transparent
            )
        )
    }
}
