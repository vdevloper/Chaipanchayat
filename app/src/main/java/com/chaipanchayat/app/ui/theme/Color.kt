package com.chaipanchayat.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Spec v2.0 Editorial Color System
val ChaiSaffron = Color(0xFFFF5A00) // Chai Orange #FF5A00
val ChaiSaffronDark = Color(0xFFD94800)
val ChaiSaffronTintLight = Color(0xFFFFF0E6)
val ChaiSaffronTintDark = Color(0xFF331604)
val ChaiCrimson = Color(0xFFE52B2B) // Breaking Red #E52B2B
val ChaiCrimsonDark = Color(0xFFB91C1C)
val ChaiAmber = Color(0xFFFF9E00)
val ChaiGold = Color(0xFFD9A441) // Editorial Gold #D9A441
val ChaiEmerald = Color(0xFF10B981)

// Gradient Brushes
val ChaiBrandGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFFF5A00), Color(0xFFE52B2B))
)

val ChaiAmberGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFD9A441), Color(0xFFFF5A00))
)

val ChaiLiveGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFE52B2B), Color(0xFFB91C1C))
)

val ChaiHeroOverlayGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0x00000000),
        Color(0x33090A0D),
        Color(0xCC090A0D),
        Color(0xF5090A0D)
    )
)

// Premium Light Editorial Palette (Warm Newspaper Ivory & Editorial Contrast)
val LightSurface = Color(0xFFFBF9F5)
val LightSurfaceSecondary = Color(0xFFFFFFFF)
val LightSurfaceTertiary = Color(0xFFF4F1EA)
val LightOnSurface = Color(0xFF141416)
val LightMuted = Color(0xFF707784)
val LightBorder = Color(0xFFE5E0D8)
val LightSkeleton = Color(0xFFEFECE6)
val LightCardGlow = Color(0x0DFF5A00)
val ErrorBreakingNews = Color(0xFFE52B2B)

// Spec v2.0 Obsidian Dark Palette
val DarkSurface = Color(0xFF090A0D) // Background #090A0D
val DarkSurfaceSecondary = Color(0xFF11141A) // Surface #11141A
val DarkSurfaceTertiary = Color(0xFF181B22) // Elevated #181B22
val DarkOnSurface = Color(0xFFF5F2EC) // Primary Text #F5F2EC
val DarkSecondaryText = Color(0xFF9EA3AE) // Secondary Text #9EA3AE
val DarkMuted = Color(0xFF707784) // Muted Text #707784
val DarkBorder = Color(0xFF252A34) // Border #252A34
val DarkSkeleton = Color(0xFF181B22)
val DarkCardGlow = Color(0x14FF5A00)

// World-Class Liquid Glass Palette (Refractive, Frosted, Specular)
val LiquidGlassBackground = Color(0xFF060911)
val LiquidGlassSurface = Color(0xD90E1626)
val LiquidGlassSurfaceSecondary = Color(0xCC152138)
val LiquidGlassSurfaceTertiary = Color(0xA61E2E4C)
val LiquidGlassOnSurface = Color(0xFFF1F5F9)
val LiquidGlassMuted = Color(0xFF94A3B8)
val LiquidGlassBorderColor = Color(0x4038BDF8)
val LiquidGlassSkeleton = Color(0x801B273E)
val LiquidGlassGlow = Color(0x3800F0FF)
val LiquidGlassCyan = Color(0xFF00E5FF)

// Glass Optical Refraction Brushes
val LiquidGlassBorderBrush = Brush.linearGradient(
    colors = listOf(
        Color(0x80FFFFFF),
        Color(0x20FFFFFF),
        Color(0x5938BDF8),
        Color(0x4DFF5400),
        Color(0x1A00E5FF)
    )
)

val LiquidGlassCardSpecularBrush = Brush.verticalGradient(
    colors = listOf(
        Color(0x2EFFFFFF),
        Color(0x08FFFFFF),
        Color(0x00000000)
    )
)

val LiquidGlassHeroOverlay = Brush.verticalGradient(
    colors = listOf(
        Color(0x00000000),
        Color(0x40060911),
        Color(0xD9060911),
        Color(0xF2060911)
    )
)

