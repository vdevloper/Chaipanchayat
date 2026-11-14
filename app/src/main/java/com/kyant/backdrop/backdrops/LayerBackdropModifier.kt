package com.kyant.backdrop.backdrops

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import com.kyant.backdrop.internal.recordLayer

fun Modifier.layerBackdrop(backdrop: LayerBackdrop): Modifier =
    this then LayerBackdropElement(backdrop)

private class LayerBackdropElement(
    val backdrop: LayerBackdrop
) : ModifierNodeElement<LayerBackdropNode>() {

    override fun create(): LayerBackdropNode {
        return LayerBackdropNode(backdrop)
    }

    override fun update(node: LayerBackdropNode) {
        if (node.backdrop != backdrop) {
            node.backdrop.layerCoordinates = null
            node.backdrop = backdrop
        }
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "layerBackdrop"
        properties["backdrop"] = backdrop
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LayerBackdropElement) return false

        if (backdrop != other.backdrop) return false

        return true
    }

    override fun hashCode(): Int {
        return backdrop.hashCode()
    }
}

private class LayerBackdropNode(
    var backdrop: LayerBackdrop
) : DelegatingNode(), DrawModifierNode, GlobalPositionAwareModifierNode, NestedScrollConnection {

    init {
        delegate(nestedScrollModifierNode(this, null))
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        if (consumed.x != 0f || consumed.y != 0f) {
            backdrop.drawVersion++
        }
        return Offset.Zero
    }

    override fun ContentDrawScope.draw() {
        // Observe drawVersion so this LayerBackdropNode re-records whenever child scroll containers scroll
        val currentVersion = backdrop.drawVersion
        if (currentVersion >= 0 && size.isSpecified && size.width > 0f && size.height > 0f) {
            backdrop.isRecording = true
            try {
                recordLayer(this, backdrop.graphicsLayer) {
                    with(backdrop) {
                        this@draw.onDraw()
                    }
                }
                if (!backdrop.isRecorded) {
                    backdrop.isRecorded = true
                }
            } catch (_: Throwable) {
                backdrop.isRecorded = false
            } finally {
                backdrop.isRecording = false
            }

            if (backdrop.isRecorded) {
                try {
                    drawLayer(backdrop.graphicsLayer)
                } catch (_: Throwable) {
                    drawContent()
                }
            } else {
                drawContent()
            }
        } else {
            backdrop.isRecorded = false
            drawContent()
        }
    }

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (coordinates.isAttached) {
            backdrop.layerCoordinates = coordinates
            backdrop.drawVersion++
        }
    }

    override fun onDetach() {
        backdrop.layerCoordinates = null
        backdrop.isRecorded = false
    }
}
