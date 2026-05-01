package com.zedalpha.shadowgadgets.compose.internal

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize

internal class LayerNode(private val shadowNode: ShadowNode) :
    Modifier.Node(),
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    override val shouldAutoInvalidate: Boolean get() = false

    private lateinit var layer: GraphicsLayer

    var color: Color = Color.Unspecified
        set(value) {
            if (field == value) return
            field = value
            layer.setTint(value)
        }

    override fun onAttach() = obtainLayer()

    override fun onDetach() = releaseLayer()

    private var positionOnScreen = Offset.Unspecified
    private var positionInLayer = Offset.Unspecified
    private var layerSize = IntSize.Zero

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val rootCoordinates = coordinates.findRootCoordinates()
        val positionOnScreen = rootCoordinates.positionOnScreen()
        val positionInLayer = coordinates.positionInRoot()
        val layerSize = rootCoordinates.size

        when {
            layer.compositingStrategy == CompositingStrategy.Offscreen &&
                    this.positionOnScreen != positionOnScreen -> {
                releaseLayer()
                obtainLayer()
                shadowNode.invalidateDraw()
            }

            this.positionInLayer != positionInLayer ||
                    this.layerSize != layerSize -> {
                shadowNode.invalidateDraw()
            }
        }

        this.positionOnScreen = positionOnScreen
        this.positionInLayer = positionInLayer
        this.layerSize = layerSize
    }

    private fun obtainLayer() {
        val layer = requireGraphicsContext().createGraphicsLayer()
        layer.setTint(color)
        this.layer = layer
    }

    private fun releaseLayer() =
        requireGraphicsContext().releaseGraphicsLayer(layer)

    fun draw(scope: DrawScope) {
        val positionInLayer = this.positionInLayer
        val shadowNode = this.shadowNode
        val layer = this.layer

        val size: IntSize
        val offset: Offset
        if (positionInLayer.isSpecified) {
            size = layerSize
            offset = positionInLayer
        } else {
            // Fallback values are from View.gatherTransparentRegion().
            val elevation = shadowNode.shadowElevation
            val width = scope.size.width + 2 * elevation
            val height = scope.size.height + 4 * elevation
            size = Size(width, height).toIntSize()
            offset = Offset(elevation, elevation)
        }

        layer.record(scope, scope.layoutDirection, size) {
            translate(offset.x, offset.y) {
                shadowNode.drawShadow(this)
            }
        }
        scope.translate(-offset.x, -offset.y) {
            drawLayer(layer)
        }
    }
}

private fun GraphicsLayer.setTint(color: Color) =
    if (color.isTint) {
        this.alpha = 1F
        this.colorFilter =
            ColorMatrixColorFilter(
                ColorMatrix(
                    floatArrayOf(
                        0F, 0F, 0F, 0F, 255 * color.red,
                        0F, 0F, 0F, 0F, 255 * color.green,
                        0F, 0F, 0F, 0F, 255 * color.blue,
                        0F, 0F, 0F, color.alpha, 0F
                    )
                )
            )
        this.compositingStrategy = CompositingStrategy.Offscreen
    } else {
        this.alpha = color.alpha
        this.colorFilter = null
        // Technically we should check isClipped, but the only unclipped setup
        // that reaches here is a lambda shadowCompat tinted black, which should
        // be just a temporary state, otherwise shadowCompat is kinda pointless.
        // It's not worth it to pass that value through for only that edge case.
        this.compositingStrategy =
            if (color.isDefault && ClipRequiresOffscreenLayer) {
                CompositingStrategy.Offscreen
            } else {
                CompositingStrategy.ModulateAlpha
            }
    }

internal val ClipRequiresLayer = Build.VERSION.SDK_INT in 24..28

internal val ClipRequiresOffscreenLayer = Build.VERSION.SDK_INT == 24