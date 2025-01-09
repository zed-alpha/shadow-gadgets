package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawModifierNode
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
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.unit.IntSize

internal class CompositingLayerModifierNode(
    private val drawNode: CacheDrawModifierNode
) : Modifier.Node(), GlobalPositionAwareModifierNode {

    private var colorLayer: GraphicsLayer? = null

    private var _color = Color.Unspecified

    var color: Color
        get() = _color
        set(value) {
            if (_color == value) return
            _color = value
            colorLayer?.setColorFilter(value)
        }

    fun resetColor() {
        _color = Color.Unspecified
    }

    override val shouldAutoInvalidate: Boolean = false

    override fun onAttach() {
        colorLayer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }
        colorLayer = requireGraphicsContext().createGraphicsLayer()
    }

    override fun onDetach() {
        colorLayer?.let {
            requireGraphicsContext().releaseGraphicsLayer(it)
            colorLayer = null
        }
    }

    private var rootSize = IntSize.Zero
    private var positionInRoot = Offset.Unspecified
    private var positionOnScreen = Offset.Unspecified

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val layer = colorLayer ?: return

        val rootCoordinates = coordinates.findRootCoordinates()
        val rootSize = rootCoordinates.size
        val positionInRoot = coordinates.positionInRoot()
        val positionOnScreen = rootCoordinates.positionOnScreen()

        when {
            color.isNotDefault && this.positionOnScreen != positionOnScreen -> {
                val graphics = requireGraphicsContext()
                graphics.releaseGraphicsLayer(layer)
                colorLayer = graphics.createGraphicsLayer()
                resetColor()
                drawNode.invalidateDraw()
            }
            this.rootSize != rootSize ||
                    this.positionInRoot != positionInRoot -> {
                drawNode.invalidateDraw()
            }
        }

        this.rootSize = rootSize
        this.positionInRoot = positionInRoot
        this.positionOnScreen = positionOnScreen
    }

    fun draw(scope: DrawScope, drawShadow: DrawScope.() -> Unit) = with(scope) {
        val layer = colorLayer ?: return
        val offset = positionInRoot
        layer.record(rootSize) { translate(offset.x, offset.y, drawShadow) }
        translate(-offset.x, -offset.y) { drawLayer(layer) }
    }
}

private fun GraphicsLayer.setColorFilter(color: Color) {
    if (color.isNotDefault) {
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
        alpha = 1F
        colorFilter = null
        compositingStrategy = CompositingStrategy.ModulateAlpha
    }
}