package com.kyant.backdrop.backdrops

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Density
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.internal.InverseLayerScope

private val DefaultOnDraw: ContentDrawScope.() -> Unit = { drawContent() }

@Composable
fun rememberLayerBackdrop(
    graphicsLayer: GraphicsLayer = rememberGraphicsLayer(),
    onDraw: ContentDrawScope.() -> Unit = DefaultOnDraw
): LayerBackdrop {
    return remember(graphicsLayer, onDraw) {
        LayerBackdrop(graphicsLayer, onDraw)
    }
}

@Stable
class LayerBackdrop internal constructor(
    val graphicsLayer: GraphicsLayer,
    internal val onDraw: ContentDrawScope.() -> Unit
) : Backdrop {

    override val isCoordinatesDependent: Boolean = true

    internal var layerCoordinates: LayoutCoordinates? by mutableStateOf(null)
    internal var isRecorded: Boolean = false
    internal var isRecording: Boolean = false

    private var inverseLayerScope: InverseLayerScope? = null

    override fun DrawScope.drawBackdrop(
        density: Density,
        coordinates: LayoutCoordinates?,
        layerBlock: (GraphicsLayerScope.() -> Unit)?
    ) {
        if (!isRecorded || isRecording) return
        val coordinates = coordinates ?: return
        val layerCoordinates = layerCoordinates ?: return
        if (!coordinates.isAttached || !layerCoordinates.isAttached) return

        try {
            withTransform({
                if (layerBlock != null) {
                    with(obtainInverseLayerScope()) { inverseTransform(density, layerBlock) }
                }
                val offset =
                    try {
                        if (layerCoordinates.isAttached && coordinates.isAttached) {
                            layerCoordinates.localPositionOf(coordinates)
                        } else {
                            androidx.compose.ui.geometry.Offset.Zero
                        }
                    } catch (_: Exception) {
                        try {
                            if (coordinates.isAttached && layerCoordinates.isAttached) {
                                coordinates.positionInWindow() - layerCoordinates.positionInWindow()
                            } else {
                                androidx.compose.ui.geometry.Offset.Zero
                            }
                        } catch (_: Exception) {
                            androidx.compose.ui.geometry.Offset.Zero
                        }
                    }
                translate(-offset.x, -offset.y)
            }) {
                drawLayer(graphicsLayer)
            }
        } catch (_: Throwable) {
            // Graceful fallback: never crash if graphics layer sampling fails during recomposition
        }
    }

    private fun obtainInverseLayerScope(): InverseLayerScope {
        return inverseLayerScope?.apply { reset() }
            ?: InverseLayerScope().also { inverseLayerScope = it }
    }
}
