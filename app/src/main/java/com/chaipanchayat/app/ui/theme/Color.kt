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

// Premium Light Editorial Palette (Warm Newspaper Ivory & Ultra-High Editorial Contrast)
val LightSurface = Color(0xFFF8F9FA)
val LightSurfaceSecondary = Color(0xFFFFFFFF)
val LightSurfaceTertiary = Color(0xFFF1F5F9)
val LightOnSurface = Color(0xFF0F172A) // Deep Slate-900 for ultra-crisp readable headlines
val LightSecondaryText = Color(0xFF334155) // Slate-700 for high-contrast bylines and excerpts
val LightMuted = Color(0xFF475569) // Slate-600 (contrast > 7:1) for readable dates/meta
val LightBorder = Color(0xFFE2E8F0)
val LightSkeleton = Color(0xFFE2E8F0)
val LightCardGlow = Color(0x0DFF5A00)
val LightBrandText = Color(0xFFC2410C) // Burnt saffron (contrast > 5.3:1 on white) for tags
val LightGoldText = Color(0xFF854D0E) // Deep gold (contrast > 6.2:1 on white) for badges
val ErrorBreakingNews = Color(0xFFE52B2B)

// Spec v2.0 Obsidian Dark Palette (High-Contrast, Eye-Friendly Night Reading)
val DarkSurface = Color(0xFF090B10) // Deep pure canvas
val DarkSurfaceSecondary = Color(0xFF131722) // Elevated card background
val DarkSurfaceTertiary = Color(0xFF1C2232) // Higher elevation
val DarkOnSurface = Color(0xFFF8FAFC) // Slate-50 crisp white for maximum legibility
val DarkSecondaryText = Color(0xFFCBD5E1) // Slate-300 (contrast > 11:1) for subtitles
val DarkMuted = Color(0xFFA1A1AA) // Zinc-400 (contrast > 7:1) for clear readable timestamps
val DarkBorder = Color(0xFF283042) // Crisp card border
val DarkSkeleton = Color(0xFF181E2C)
val DarkCardGlow = Color(0x18FF5A00)
val DarkBrandText = Color(0xFFFF8533) // Radiant saffron (contrast > 8:1 on dark)
val DarkGoldText = Color(0xFFFBBF24) // Radiant gold (contrast > 9:1 on dark)

// World-Class Liquid Glass Palette (Refractive, Frosted, Specular)
val LiquidGlassBackground = Color(0xFF060911)
val LiquidGlassSurface = Color(0xD90E1626)
val LiquidGlassSurfaceSecondary = Color(0xCC152138)
val LiquidGlassSurfaceTertiary = Color(0xA61E2E4C)
val LiquidGlassOnSurface = Color(0xFFFFFFFF)
val LiquidGlassSecondaryText = Color(0xFFE2E8F0)
val LiquidGlassMuted = Color(0xFFCBD5E1)
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

