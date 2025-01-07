package com.zedalpha.shadowgadgets.compose

import android.os.Build
import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ColorMatrixColorFilter
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.layer.setOutline
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import com.zedalpha.shadowgadgets.core.DefaultAmbientShadowAlpha
import com.zedalpha.shadowgadgets.core.DefaultSpotShadowAlpha
import com.zedalpha.shadowgadgets.core.blendShadowColors
import com.zedalpha.shadowgadgets.core.layer.DefaultInlineLayerRequired
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
) : DelegatingNode(),
    CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode,
    ObserverModifierNode {

    private var shadowLayer: GraphicsLayer? = null

    override val shouldAutoInvalidate: Boolean = false

    override fun onAttach() {
        shadowLayer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }
        colorLayer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }

        shadowLayer = requireGraphicsContext().createGraphicsLayer()
        appliedSize = Size.Unspecified
        drawNode.invalidateDrawCache()

        onObservedReadsChanged()
    }

    private var shadowAlphas: Pair<Float, Float> =
        DefaultAmbientShadowAlpha to DefaultSpotShadowAlpha

    override fun onObservedReadsChanged() {
        observeReads {
            currentValueOf(LocalConfiguration)
            val context = currentValueOf(LocalContext)
            shadowAlphas = context.resolveThemeShadowAlphas()
            update()
        }
    }

    override fun onDetach() {
        shadowLayer?.run {
            requireGraphicsContext().releaseGraphicsLayer(this)
            shadowLayer = null
        }
        colorLayer?.run {
            requireGraphicsContext().releaseGraphicsLayer(this)
            colorLayer = null
        }
    }

    private val drawNode = delegate(CacheDrawModifierNode(::cacheDraw))

    private var actualAmbientColor = Color.Unspecified
    private var actualSpotColor = Color.Unspecified
    private var layerColor = Color.Unspecified
    private var blendAmbientColor = Color.Unspecified
    private var blendSpotColor = Color.Unspecified

    private var appliedElevation = Dp.Unspecified
    private var appliedAmbientColor = Color.Unspecified
    private var appliedSpotColor = Color.Unspecified
    private var appliedLayerColor = Color.Unspecified
    private var appliedShape = RectangleShape
    private var appliedSize = Size.Unspecified
    private var appliedLayoutDirection = LayoutDirection.Ltr

    fun update() {
        val useNative = Build.VERSION.SDK_INT >= 28 && !forceColorCompat
        actualAmbientColor = if (useNative) ambientColor else DefaultShadowColor
        actualSpotColor = if (useNative) spotColor else DefaultShadowColor
        layerColor = when {
            useNative || colorCompat.isDefault ||
                    (colorCompat.isUnspecified &&
                            ambientColor.isDefault && spotColor.isDefault) -> {
                if (clipped && DefaultInlineLayerRequired) DefaultShadowColor
                else Color.Unspecified
            }

            colorCompat.isSpecified -> colorCompat

            blendAmbientColor != ambientColor || blendSpotColor != spotColor -> {
                blendAmbientColor = ambientColor; blendSpotColor = spotColor
                val ambient = ambientColor.toArgb()
                val spot = spotColor.toArgb()
                val (ambientAlpha, spotAlpha) = shadowAlphas
                Color(blendShadowColors(ambient, ambientAlpha, spot, spotAlpha))
            }

            else -> appliedLayerColor
        }

        when {
            appliedShape != shape || appliedLayerColor != layerColor -> {
                drawNode.invalidateDrawCache()
            }
            appliedElevation != elevation ||
                    appliedAmbientColor != actualAmbientColor ||
                    appliedSpotColor != actualSpotColor -> {
                drawNode.invalidateDraw()
            }
        }
    }

    private var rootSize = IntSize.Zero
    private var positionInRoot = Offset.Unspecified
    private var positionOnScreen = Offset.Unspecified

    private var appliedRootSize = IntSize.Zero
    private var appliedPositionInRoot = Offset.Unspecified
    private var appliedPositionOnScreen = Offset.Unspecified

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        if (!layerColor.isSpecified) return

        val rootCoordinates = coordinates.findRootCoordinates()

        rootSize = rootCoordinates.size
        positionInRoot = coordinates.positionInRoot()
        positionOnScreen = rootCoordinates.positionOnScreen()

        when {
            !layerColor.isDefault &&
                    appliedPositionOnScreen != positionOnScreen -> {
                drawNode.invalidateDrawCache()
            }
            appliedRootSize != rootSize ||
                    appliedPositionInRoot != positionInRoot -> {
                drawNode.invalidateDraw()
            }
        }
    }

    private var colorLayer: GraphicsLayer? = null

    private val clipPath = if (clipped) Path() else null

    private fun cacheDraw(scope: CacheDrawScope): DrawResult = with(scope) {
        if (appliedShape != shape || appliedSize != size ||
            appliedLayoutDirection != layoutDirection
        ) {
            val outline = shape.createOutline(size, layoutDirection, this)
            shadowLayer?.run { setOutline(outline); record { } }
            clipPath?.run { rewind(); addOutline(outline) }

            appliedShape = shape
            appliedSize = size
            appliedLayoutDirection = layoutDirection
        }

        val currentLayer = colorLayer
        val graphics = requireGraphicsContext()
        val nextLayer = when {
            layerColor.isUnspecified -> null.also {
                currentLayer?.let { graphics.releaseGraphicsLayer(it) }
            }

            currentLayer == null -> graphics.createGraphicsLayer()

            else -> if (appliedPositionOnScreen != positionOnScreen) {
                graphics.releaseGraphicsLayer(currentLayer)
                graphics.createGraphicsLayer()
            } else {
                currentLayer
            }
        }
        if (nextLayer != null &&
            (nextLayer != currentLayer || appliedLayerColor != layerColor)
        ) {
            nextLayer.setColorFilter(layerColor)
        }
        colorLayer = nextLayer

        appliedPositionOnScreen = positionOnScreen
        appliedLayerColor = layerColor

        onDrawBehind {
            shadowLayer?.let { shadow ->

                shadow.shadowElevation = elevation.toPx()
                shadow.ambientShadowColor = actualAmbientColor
                shadow.spotShadowColor = actualSpotColor

                val layer = colorLayer
                if (layer != null) {
                    val offset = positionInRoot
                    layer.record(this@with, size = rootSize) {
                        translate(offset.x, offset.y) { drawShadow(shadow) }
                    }
                    translate(-offset.x, -offset.y) { drawLayer(layer) }
                } else {
                    drawShadow(shadow)
                }
            }

            appliedElevation = elevation
            appliedAmbientColor = actualAmbientColor
            appliedSpotColor = actualSpotColor
            appliedPositionInRoot = positionInRoot
            appliedRootSize = rootSize
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

private fun GraphicsLayer.setColorFilter(color: Color) {
    if (color.isDefault) {
        alpha = 1F
        colorFilter = null
        compositingStrategy = CompositingStrategy.ModulateAlpha
    } else {
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
    }
}

internal inline val Color.isDefault: Boolean get() = this == DefaultShadowColor