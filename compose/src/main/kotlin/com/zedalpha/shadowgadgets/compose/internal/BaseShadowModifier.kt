package com.zedalpha.shadowgadgets.compose.internal

import android.os.Build
import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.layer.setOutline
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireView
import androidx.compose.ui.unit.Dp
import com.zedalpha.shadowgadgets.core.blendShadowColors
import com.zedalpha.shadowgadgets.core.layer.RequiresDefaultClipLayer
import com.zedalpha.shadowgadgets.core.resolveThemeShadowAlphas

internal abstract class BaseShadowElement(
    val elevation: Dp,
    val shape: Shape,
    val ambientColor: Color,
    val spotColor: Color,
    val colorCompat: Color,
    val forceColorCompat: Boolean
) : ModifierNodeElement<BaseShadowNode>() {

    override fun update(node: BaseShadowNode) {
        node.elevation = elevation
        node.shape = shape
        node.ambientColor = ambientColor
        node.spotColor = spotColor
        node.colorCompat = colorCompat
        node.forceColorCompat = forceColorCompat
        node.update()
    }
}

internal class BaseShadowNode(
    private val clipped: Boolean,
    var elevation: Dp,
    var shape: Shape,
    var ambientColor: Color,
    var spotColor: Color,
    var colorCompat: Color,
    var forceColorCompat: Boolean
) : DelegatingNode() {

    private val drawNode = delegate(CacheDrawModifierNode(::cacheDraw))

    private var layerNode: CompositingLayerModifierNode? = null

    override val shouldAutoInvalidate: Boolean = false

    override fun onAttach() {
        layerNode?.resetColor()
        currentShape = null
        update()
    }

    private var actualAmbientColor = Color.Unspecified
    private var actualSpotColor = Color.Unspecified
    private var layerColor = Color.Unspecified

    private var colorBlender: ColorBlender? = null

    private var currentShape: Shape? = null
    private var currentElevation = Dp.Unspecified
    private var currentAmbientColor = Color.Unspecified
    private var currentSpotColor = Color.Unspecified
    private var currentLayerColor = Color.Unspecified

    fun update() {
        val useNative = Build.VERSION.SDK_INT >= 28 && !forceColorCompat
        actualAmbientColor = if (useNative) ambientColor else DefaultShadowColor
        actualSpotColor = if (useNative) spotColor else DefaultShadowColor
        layerColor = when {
            useNative || colorCompat.isDefault ||
                    (colorCompat.isUnspecified && // Will blend to default
                            ambientColor.isDefault && spotColor.isDefault) -> {
                if (clipped && RequiresDefaultClipLayer) DefaultShadowColor
                else Color.Unspecified
            }

            colorCompat.isSpecified -> colorCompat

            else -> {
                val blender = colorBlender
                    ?: ColorBlender(this).also { colorBlender = it }
                blender.blend(ambientColor, spotColor)
            }
        }

        when {
            layerColor.isUnspecified -> {
                layerNode?.also { undelegate(it); layerNode = null }
            }
            layerNode == null -> {
                CompositingLayerModifierNode(drawNode)
                    .also { delegate(it); layerNode = it }
            }
        }

        when {
            currentShape != shape -> drawNode.invalidateDrawCache()

            currentElevation != elevation ||
                    currentAmbientColor != actualAmbientColor ||
                    currentSpotColor != actualSpotColor ||
                    currentLayerColor != layerColor -> drawNode.invalidateDraw()
        }
    }

    private val clipPath = if (clipped) Path() else null

    private fun cacheDraw(scope: CacheDrawScope): DrawResult = with(scope) {

        val shadow = obtainGraphicsLayer()

        val outline = shape.createOutline(size, layoutDirection, this)
        shadow.run { setOutline(outline); record { } }
        clipPath?.run { rewind(); addOutline(outline) }

        currentShape = shape

        onDrawBehind {

            shadow.shadowElevation = elevation.toPx()
            shadow.ambientShadowColor = actualAmbientColor
            shadow.spotShadowColor = actualSpotColor

            val layer = layerNode
            if (layer != null) {
                layer.color = layerColor
                layer.draw(this) { drawShadow(shadow) }
            } else {
                drawShadow(shadow)
            }

            currentElevation = elevation
            currentAmbientColor = actualAmbientColor
            currentSpotColor = actualSpotColor
            currentLayerColor = layerColor
        }
    }

    private fun DrawScope.drawShadow(shadow: GraphicsLayer) {
        val path = clipPath
        if (path != null) {
            clipPath(path, ClipOp.Difference) { drawLayer(shadow) }
        } else {
            drawLayer(shadow)
        }
    }
}

private class ColorBlender(private val node: DelegatableNode) {

    private var ambientColor = Color.Unspecified
    private var spotColor = Color.Unspecified
    private var blendedColor = Color.Unspecified

    fun blend(ambientColor: Color, spotColor: Color): Color =
        if (this.ambientColor != ambientColor || this.spotColor != spotColor) {
            this.ambientColor = ambientColor; this.spotColor = spotColor
            val ambient = ambientColor.toArgb()
            val spot = spotColor.toArgb()
            val context = node.requireView().context
            val (ambientAlpha, spotAlpha) = context.resolveThemeShadowAlphas()
            val argb = blendShadowColors(ambient, ambientAlpha, spot, spotAlpha)
            Color(argb).also { blendedColor = it }
        } else {
            blendedColor
        }
}

internal inline val Color.isDefault get() = this == DefaultShadowColor
internal inline val Color.isNotDefault get() = this != DefaultShadowColor