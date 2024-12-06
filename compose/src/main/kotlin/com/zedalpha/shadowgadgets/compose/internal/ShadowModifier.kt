package com.zedalpha.shadowgadgets.compose.internal

import android.os.Build
import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.nativeCanvas
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntSize
import androidx.core.graphics.withTranslation
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.DefaultAmbientShadowAlpha
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.DefaultSpotShadowAlpha
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.blendShadowColors
import com.zedalpha.shadowgadgets.core.layer.DefaultInlineLayerRequired
import com.zedalpha.shadowgadgets.core.layer.SingleDrawLayer
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

    private var coreShadow: Shadow? = null

    private lateinit var shadowOutline: ShadowOutline

    private lateinit var currentState: StateHolder

    override val shouldAutoInvalidate: Boolean = false

    override fun onAttach() {
        val view = currentValueOf(LocalView)
        coreShadow = if (clipped) {
            val outline = ClippedOutline().also { shadowOutline = it }
            val provider = PathProvider { it.set(outline.androidPath) }
            ClippedShadow(view).apply { pathProvider = provider }
        } else {
            shadowOutline = CompatOutline()
            Shadow(view)
        }
        currentState = StateHolder()
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

    private var layerColor = DefaultShadowColor

    fun update() {
        val current = currentState
        var invalidateDraw = false
        var invalidateCache = false

        if (current.elevation != elevation) invalidateDraw = true
        if (current.shape != shape) invalidateCache = true

        if (Build.VERSION.SDK_INT >= 28 && !forceColorCompat) {
            if (current.ambientColor != ambientColor) invalidateDraw = true
            if (current.spotColor != spotColor) invalidateDraw = true
        } else {
            val layerColor = when {
                colorCompat.isDefault || colorCompat.isUnspecified &&
                        ambientColor.isDefault && spotColor.isDefault -> {
                    DefaultShadowColor
                }

                colorCompat.isSpecified -> colorCompat

                current.ambientColor != ambientColor ||
                        current.spotColor != spotColor -> blendNativeColors()

                else -> layerColor
            }
            this.layerColor = layerColor

            if (current.layerColor.isDefault != layerColor.isDefault) {
                invalidateCache = true
            } else if (current.layerColor != layerColor) {
                invalidateDraw = true
            }
        }

        if (invalidateCache) {
            cacheDrawModifier.invalidateDrawCache()
        } else if (invalidateDraw) {
            cacheDrawModifier.invalidateDraw()
        }
    }

    private fun blendNativeColors(): Color {
        val (ambientAlpha, spotAlpha) = shadowAlphas
        val ambient = ambientColor.toArgb()
        val spot = spotColor.toArgb()
        return Color(blendShadowColors(ambient, ambientAlpha, spot, spotAlpha))
    }

    private val cacheDrawModifier = delegate(CacheDrawModifierNode(::cacheDraw))

    private var rootSize: IntSize = IntSize.Zero
    private var positionInRoot: Offset = Offset.Unspecified
    private var positionOnScreen: Offset = Offset.Unspecified
    private var recreateLayer = false

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val current = currentState
        var invalidateCache = false
        var invalidateDraw = false

        val rootSize = coordinates.findRootCoordinates().size
        if (current.rootSize != rootSize) invalidateDraw = true
        this.rootSize = rootSize

        val positionInRoot = coordinates.positionInRoot()
        if (current.positionInRoot != positionInRoot) invalidateDraw = true
        this.positionInRoot = positionInRoot

        val positionOnScreen =
            coordinates.findRootCoordinates().positionOnScreen()
        if (current.positionOnScreen != positionOnScreen) {
            invalidateCache = true
            recreateLayer = true
        }
        this.positionOnScreen = positionOnScreen

        if (invalidateCache) {
            cacheDrawModifier.invalidateDrawCache()
        } else if (invalidateDraw) {
            cacheDrawModifier.invalidateDraw()
        }
    }

    private var coreLayer: SingleDrawLayer? = null

    private fun cacheDraw(scope: CacheDrawScope): DrawResult = with(scope) {
        val shadow = coreShadow ?: return@with onDrawBehind { }

        val current = currentState

        if (current.shape != shape || current.size != size ||
            current.layoutDirection != layoutDirection
        ) {
            shadowOutline.setShape(shape, size, layoutDirection, this)
            shadow.setOutline(shadowOutline.androidOutline)

            current.shape = shape
            current.size = size
            current.layoutDirection = layoutDirection
        }

        val layer =
            if (layerColor.isDefault && !DefaultInlineLayerRequired) {
                coreLayer?.run { dispose(); coreLayer = null }
                null
            } else {
                coreLayer?.apply { if (recreateLayer) recreate() }
                    ?: createLayer(shadow).also { coreLayer = it }
            }
        recreateLayer = false

        onDrawBehind {
            shadow.elevation = elevation.toPx()

            val intSize = size.roundToIntSize()
            val canvas = drawContext.canvas.nativeCanvas

            if (layer != null) {
                layer.setSize(rootSize.width, rootSize.height)

                val rootPosition = positionInRoot.round()
                shadow.setPosition(
                    rootPosition.x,
                    rootPosition.y,
                    rootPosition.x + intSize.width,
                    rootPosition.y + intSize.height
                )

                shadow.ambientColor = DefaultShadowColorInt
                shadow.spotColor = DefaultShadowColorInt
                layer.color = layerColor.toArgb()

                layer.refresh()
                val offset = positionInRoot
                canvas.withTranslation(-offset.x, -offset.y, layer::draw)
            } else {
                shadow.setPosition(0, 0, intSize.width, intSize.height)
                shadow.ambientColor = ambientColor.toArgb()
                shadow.spotColor = spotColor.toArgb()
                shadow.draw(canvas)
            }

            current.elevation = elevation
            current.ambientColor = ambientColor
            current.spotColor = spotColor
            current.layerColor = layerColor
            current.rootSize = rootSize
            current.positionInRoot = positionInRoot
            current.positionOnScreen = positionOnScreen
        }
    }

    private fun createLayer(shadow: Shadow) =
        SingleDrawLayer(currentValueOf(LocalView), 0, 0, 0, shadow::draw)

    override fun onDetach() {
        coreShadow?.run { dispose(); coreShadow = null }
        coreLayer?.run { dispose(); coreLayer = null }
    }
}

private class StateHolder(
    var elevation: Dp = Dp.Unspecified,
    var ambientColor: Color = DefaultShadowColor,
    var spotColor: Color = DefaultShadowColor,
    var layerColor: Color = DefaultShadowColor,
    var rootSize: IntSize = IntSize.Zero,
    var positionInRoot: Offset = Offset.Unspecified,
    var positionOnScreen: Offset = Offset.Unspecified,
    var shape: Shape = RectangleShape,
    var size: Size = Size.Unspecified,
    var layoutDirection: LayoutDirection? = null
)

internal inline val Color.isDefault: Boolean
    get() = this == DefaultShadowColor