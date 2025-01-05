package com.zedalpha.shadowgadgets.compose.internal

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

internal abstract class ShadowElement(
    val elevation: Dp,
    val shape: Shape,
    val ambientColor: Color,
    val spotColor: Color,
    val colorCompat: Color,
    val forceColorCompat: Boolean
) : ModifierNodeElement<ShadowNode>() {

    override fun update(node: ShadowNode) {
        node.elevation = elevation
        node.shape = shape
        node.ambientColor = ambientColor
        node.spotColor = spotColor
        node.colorCompat = colorCompat
        node.forceColorCompat = forceColorCompat
        node.update()
    }
}

internal class ShadowNode(
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
        val graphics = requireGraphicsContext()

        shadowLayer?.let { graphics.releaseGraphicsLayer(it) }
        colorLayer?.let { graphics.releaseGraphicsLayer(it) }

        shadowLayer = graphics.createGraphicsLayer()
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
            appliedShape != shape ||
                    appliedLayerColor.isSpecified != layerColor.isSpecified -> {
                drawNode.invalidateDrawCache()
            }
            appliedElevation != elevation ||
                    appliedAmbientColor != actualAmbientColor ||
                    appliedSpotColor != actualSpotColor ||
                    appliedLayerColor != layerColor -> {
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
        rootSize = coordinates.findRootCoordinates().size
        positionInRoot = coordinates.positionInRoot()
        positionOnScreen = coordinates.findRootCoordinates().positionOnScreen()

        if (appliedPositionOnScreen != positionOnScreen ||
            appliedLayerColor.isSpecified &&
            (appliedRootSize != rootSize ||
                    appliedPositionInRoot != positionInRoot)
        ) {
            drawNode.invalidateDrawCache()
        }
    }

    private val clipPath = if (clipped) Path() else null

    private var colorLayer: GraphicsLayer? = null

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
        nextLayer?.let { layer ->
            layer.record(size = rootSize) {
                val shadow = shadowLayer ?: return@record

                val offset = positionInRoot
                translate(offset.x, offset.y) { drawShadow(shadow) }
            }
        }
        colorLayer = nextLayer

        appliedAmbientColor = actualAmbientColor
        appliedSpotColor = actualSpotColor
        appliedLayerColor = layerColor
        appliedRootSize = rootSize
        appliedPositionInRoot = positionInRoot
        appliedPositionOnScreen = positionOnScreen
        appliedElevation = elevation

        onDrawBehind {
            val shadow = shadowLayer ?: return@onDrawBehind

            shadow.ambientShadowColor = actualAmbientColor
            shadow.spotShadowColor = actualSpotColor

            val layer = colorLayer
            if (layer != null) {
                layer.setLayerFilter(layerColor)

                val offset = positionInRoot
                translate(-offset.x, -offset.y) { drawLayer(layer) }
            } else {
                drawShadow(shadow)
            }
        }
    }

    private fun DrawScope.drawShadow(shadow: GraphicsLayer) {
        shadow.shadowElevation = elevation.toPx()

        val path = clipPath
        if (path != null) {
            clipPath(path, ClipOp.Difference) { drawLayer(shadow) }
        } else {
            drawLayer(shadow)
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
}

private fun GraphicsLayer.setLayerFilter(color: Color) {
    if (color == DefaultShadowColor) {
        alpha = 1F
        colorFilter = null
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
    }
}

internal inline val Color.isDefault: Boolean get() = this == DefaultShadowColor