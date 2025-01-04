package com.zedalpha.shadowgadgets.compose.internal

import android.graphics.Color.TRANSPARENT
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
import androidx.compose.ui.node.requireView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.roundToIntSize
import androidx.core.graphics.withTranslation
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.DefaultAmbientShadowAlpha
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

    override val shouldAutoInvalidate: Boolean = false

    override fun onAttach() {
        coreShadow = if (clipped) {
            val outline = ClippedOutline().also { shadowOutline = it }
            val provider = PathProvider { it.set(outline.androidPath) }
            ClippedShadow(requireView()).apply { pathProvider = provider }
        } else {
            shadowOutline = ShadowOutline()
            Shadow(requireView())
        }
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

    private var appliedElevation: Dp = Dp.Unspecified
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

        when {
            appliedPositionOnScreen != positionOnScreen -> {
                drawNode.invalidateDrawCache()
            }
            appliedRootSize != rootSize ||
                    appliedPositionInRoot != positionInRoot -> {
                drawNode.invalidateDraw()
            }
        }
    }

    private var coreLayer: SingleDrawLayer? = null

    private fun cacheDraw(scope: CacheDrawScope): DrawResult = with(scope) {
        if (appliedShape != shape || appliedSize != size ||
            appliedLayoutDirection != layoutDirection
        ) {
            val outline = shadowOutline
            outline.setShape(shape, size, layoutDirection, this)
            coreShadow?.setOutline(outline.androidOutline)

            appliedShape = shape
            appliedSize = size
            appliedLayoutDirection = layoutDirection
        }

        val current = coreLayer
        coreLayer = when {
            layerColor.isUnspecified -> null.also { current?.dispose() }

            current == null -> coreShadow?.let { shadow ->
                SingleDrawLayer(requireView(), TRANSPARENT, 0, 0, shadow::draw)
            }

            else -> current.apply {
                if (appliedPositionOnScreen != positionOnScreen) recreate()
            }
        }
        appliedPositionOnScreen = positionOnScreen

        onDrawBehind {
            val shadow = coreShadow ?: return@onDrawBehind

            shadow.elevation = elevation.toPx()
            shadow.ambientColor = actualAmbientColor.toArgb()
            shadow.spotColor = actualSpotColor.toArgb()

            val intSize = size.roundToIntSize()
            val canvas = drawContext.canvas.nativeCanvas

            val layer = coreLayer
            if (layer != null) {
                layer.setSize(rootSize.width, rootSize.height)
                layer.color = layerColor.toArgb()
                layer.refresh()

                val rootPosition = positionInRoot.round()
                shadow.setPosition(
                    rootPosition.x,
                    rootPosition.y,
                    rootPosition.x + intSize.width,
                    rootPosition.y + intSize.height
                )

                val offset = positionInRoot
                canvas.withTranslation(-offset.x, -offset.y, layer::draw)
            } else {
                shadow.setPosition(0, 0, intSize.width, intSize.height)
                shadow.draw(canvas)
            }

            appliedElevation = elevation
            appliedAmbientColor = actualAmbientColor
            appliedSpotColor = actualSpotColor
            appliedLayerColor = layerColor
            appliedRootSize = rootSize
            appliedPositionInRoot = positionInRoot
        }
    }

    override fun onDetach() {
        coreShadow?.run { dispose(); coreShadow = null }
        coreLayer?.run { dispose(); coreLayer = null }
    }
}

internal inline val Color.isDefault: Boolean get() = this == DefaultShadowColor