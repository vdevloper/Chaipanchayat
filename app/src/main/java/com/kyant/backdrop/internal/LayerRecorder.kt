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
    val layoutDirection = drawScope.layoutDirection
    val prevDrawContext = drawScope.drawContext
    val prevDensity = prevDrawContext.density
    val prevLayoutDirection = prevDrawContext.layoutDirection
    val prevCanvas = prevDrawContext.canvas
    val prevSize = prevDrawContext.size
    val prevLayer = prevDrawContext.graphicsLayer
    try {
        layer.record(density, layoutDirection, size) {
            val recordCanvas = drawContext.canvas
            val recordSize = drawContext.size
            val recordLayer = drawContext.graphicsLayer
            prevDrawContext.apply {
                this.density = density
                this.layoutDirection = layoutDirection
                this.canvas = recordCanvas
                this.size = recordSize
                this.graphicsLayer = recordLayer
            }
            recordCanvas.save()
            try {
                drawScope.block()
            } finally {
                recordCanvas.restore()
                prevDrawContext.apply {
                    this.density = prevDensity
                    this.layoutDirection = prevLayoutDirection
                    this.canvas = prevCanvas
                    this.size = prevSize
                    this.graphicsLayer = prevLayer
                }
            }
        }
    } catch (_: Throwable) {
        // Safe fallback if hardware layer recording fails
    }
}
