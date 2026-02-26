package com.zedalpha.shadowgadgets.compose.internal

import android.os.Build
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.unit.IntSize

internal class LayerNode(
    private val shadowDraw: DrawScope.() -> Unit,
    private val invalidate: () -> Unit,
) : Modifier.Node(), GlobalPositionAwareModifierNode {

    override val shouldAutoInvalidate: Boolean = false

    private lateinit var layer: GraphicsLayer

    var color: Color = Color.Unspecified
        set(value) {
            if (field == value) return
            field = value
            layer.setColorFilter(value)
        }

    override fun onAttach() {
        layer = requireGraphicsContext().createGraphicsLayer()
    }

    override fun onDetach() =
        requireGraphicsContext().releaseGraphicsLayer(layer)

    private var positionOnScreen = Offset.Unspecified
    private var positionInLayer = Offset.Unspecified
    private var layerSize = IntSize.Zero

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val rootCoordinates = coordinates.findRootCoordinates()
        val positionOnScreen = rootCoordinates.positionOnScreen()
        val positionInLayer = coordinates.positionInRoot()
        val layerSize = rootCoordinates.size

        when {
            color.isTint && this.positionOnScreen != positionOnScreen -> {
                val graphics = requireGraphicsContext()
                graphics.releaseGraphicsLayer(layer)
                layer = graphics.createGraphicsLayer()
                color = Color.Unspecified
                invalidate()
            }

            this.positionInLayer != positionInLayer ||
                    this.layerSize != layerSize -> invalidate()
        }

        this.positionOnScreen = positionOnScreen
        this.positionInLayer = positionInLayer
        this.layerSize = layerSize
    }

    fun draw(scope: DrawScope) = with(scope) {
        val layer = this@LayerNode.layer
        val offset = positionInLayer
        layer.record(layerSize) { translate(offset.x, offset.y, shadowDraw) }
        translate(-offset.x, -offset.y) { drawLayer(layer) }
    }
}

private fun GraphicsLayer.setColorFilter(color: Color) {
    if (color.isTint) {
        alpha = color.alpha
        colorFilter = ColorMatrixColorFilter(
            ColorMatrix(
                floatArrayOf(
                    0F, 0F, 0F, 0F, 255 * color.red,
                    0F, 0F, 0F, 0F, 255 * color.green,
                    0F, 0F, 0F, 0F, 255 * color.blue,
                    0F, 0F, 0F, 1F, 0F
                )
            )
        )
        compositingStrategy = CompositingStrategy.Offscreen
    } else {
        alpha = if (color.isDefault) 1F else 0F
        colorFilter = null
        compositingStrategy = CompositingStrategy.ModulateAlpha
    }
}

internal val RequiresDefaultClipLayer = Build.VERSION.SDK_INT in 24..28