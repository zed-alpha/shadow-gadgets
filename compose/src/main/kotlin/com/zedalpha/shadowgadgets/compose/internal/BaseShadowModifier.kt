package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
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
    }
}

internal abstract class BaseShadowNode(
    private val clipped: Boolean,
    elevation: Dp,
    shape: Shape,
    ambientColor: Color,
    spotColor: Color,
    colorCompat: Color,
    forceColorCompat: Boolean
) : DelegatingNode(),
    CompositionLocalConsumerModifierNode,
    GlobalPositionAwareModifierNode,
    ObserverModifierNode {

    var elevation: Dp = elevation
        set(value) {
            if (field == value) return
            field = value
            cacheDrawModifier.invalidateDraw()
        }

    var shape: Shape = shape
        set(value) {
            if (field == value) return
            field = value
            cacheDrawModifier.invalidateDrawCache()
        }

    var ambientColor: Color = ambientColor
        set(value) {
            if (field == value) return
            field = value
            cacheDrawModifier.invalidateDrawCache()
        }

    var spotColor: Color = spotColor
        set(value) {
            if (field == value) return
            field = value
            cacheDrawModifier.invalidateDrawCache()
        }

    var colorCompat: Color = colorCompat
        set(value) {
            if (field == value) return
            field = value
            cacheDrawModifier.invalidateDrawCache()
        }

    var forceColorCompat: Boolean = forceColorCompat
        set(value) {
            if (field == value) return
            field = value
            cacheDrawModifier.invalidateDrawCache()
        }

    private var composeShadow: ComposeShadow? = null

    private val cacheDrawModifier = delegate(CacheDrawModifierNode(::cacheDraw))

    private fun cacheDraw(scope: CacheDrawScope): DrawResult = with(scope) {
        val shadow = composeShadow ?: return@with onDrawBehind { }

        shadow.setShape(shape, size, layoutDirection, this)
        val compat = calculateColorCompat(colorCompat)

        onDrawBehind {
            val elev = elevation.toPx()
            val canvas = drawContext.canvas.nativeCanvas
            shadow.draw(canvas, elev, ambientColor, spotColor, compat)
        }
    }

    override val shouldAutoInvalidate: Boolean = false

    override fun onAttach() {
        composeShadow = ComposeShadow(
            currentValueOf(LocalView),
            clipped,
            colorCompat != DefaultShadowColor || DefaultInlineLayerRequired
        )
        onObservedReadsChanged()
    }

    private var size: IntSize = IntSize.Zero

    private var position: Offset = Offset.Zero

    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val size = coordinates.size
        val position = coordinates.positionInRoot()
        if (this.size == size && this.position == position) return
        this.size = size; this.position = position
        composeShadow?.setPosition(coordinates) ?: return
        cacheDrawModifier.invalidateDrawCache()
    }

    protected abstract fun calculateColorCompat(colorCompat: Color): Color

    private var alphas: Pair<Float, Float> =
        DefaultAmbientShadowAlpha to DefaultSpotShadowAlpha

    protected fun blendNativeColors(): Color {
        val (ambient, spot) = alphas
        val colorInt = blendShadowColors(
            ambientColor.toArgb(),
            ambient,
            spotColor.toArgb(),
            spot
        )
        return Color(colorInt)
    }

    override fun onObservedReadsChanged() {
        observeReads {
            currentValueOf(LocalConfiguration)
            val context = currentValueOf(LocalContext)
            alphas = context.resolveThemeShadowAlphas()
        }
    }

    override fun onDetach() {
        composeShadow?.run { dispose(); composeShadow = null }
    }
}