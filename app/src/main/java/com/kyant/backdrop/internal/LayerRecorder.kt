package com.kyant.backdrop.internal

import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize

internal fun DelegatableNode.recordLayer(
    drawScope: DrawScope,
    layer: GraphicsLayer,
    size: IntSize = if (drawScope.size.isSpecified && !drawScope.size.width.isNaN() && !drawScope.size.height.isNaN()) drawScope.size.toIntSize() else IntSize.Zero,
    block: DrawScope.() -> Unit
) {
    if (size.width <= 0 || size.height <= 0) return
    val density = runCatching { requireDensity() }.getOrNull() ?: drawScope
    try {
        layer.record(density, drawScope.layoutDirection, size) {
            val prevDensity = drawContext.density
            drawContext.density = density
            try {
                this.block()
            } finally {
                drawContext.density = prevDensity
            }
        }
    } catch (_: Throwable) {
        // Safe fallback if hardware layer recording fails
    }
}
