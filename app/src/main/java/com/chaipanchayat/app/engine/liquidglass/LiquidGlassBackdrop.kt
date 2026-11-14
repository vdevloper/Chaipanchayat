package com.chaipanchayat.app.engine.liquidglass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.chaipanchayat.app.ui.theme.ChaiTheme

/**
 * Clean, untinted studio canvas for Liquid Glass:
 * - Dark Glass Submode: Pure neutral obsidian-charcoal (#0A0B0E) with zero artificial color tints.
 * - Light Glass Submode: Pure neutral studio light grey (#F0F2F5) with zero artificial color tints.
 */
@Composable
fun LiquidGlassBackdrop(
    settings: LiquidGlassSettings,
    modifier: Modifier = Modifier
) {
    if (!ChaiTheme.extended.isLiquidGlass) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        )
        return
    }

    val baseCanvasColor = if (settings.isLightGlass) Color(0xFFF0F2F5) else Color(0xFF0A0B0E)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseCanvasColor)
    )
}
