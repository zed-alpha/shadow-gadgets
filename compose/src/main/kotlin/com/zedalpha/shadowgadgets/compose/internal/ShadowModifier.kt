package com.zedalpha.shadowgadgets.compose.internal

import android.annotation.SuppressLint
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
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.node.requireView
import androidx.compose.ui.unit.Dp

@SuppressLint("ModifierNodeInspectableProperties")
internal abstract class AbstractShadowElement(
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
) : DelegatingNode() {

    override val shouldAutoInvalidate: Boolean = false

    private lateinit var shadow: GraphicsLayer

    private var layerNode: LayerNode? = null

    override fun onAttach() {
        shadow = requireGraphicsContext().createGraphicsLayer()
        layerNode?.color = Color.Unspecified
        currentShape = null
        update()
    }

    override fun onDetach() =
        requireGraphicsContext().releaseGraphicsLayer(shadow)

    private val drawNode = delegate(CacheDrawModifierNode(::cacheDraw))

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
                    blendsToDefault(colorCompat, ambientColor, spotColor) -> {
                if (clipped && RequiresDefaultClipLayer) DefaultShadowColor
                else Color.Unspecified
            }

            colorCompat.isSpecified -> colorCompat

            else -> {
                val blender = colorBlender
                    ?: ColorBlender(requireView().context)
                        .also { colorBlender = it }
                blender.blend(ambientColor, spotColor)
            }
        }

        when {
            layerColor.isUnspecified -> {
                layerNode?.let { undelegate(it); layerNode = null }
            }

            layerNode == null -> {
                LayerNode(this::drawShadow, drawNode::invalidateDraw)
                    .let { delegate(it); layerNode = it }
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

    private val clip = if (clipped) Path() else null

    private fun cacheDraw(scope: CacheDrawScope): DrawResult = with(scope) {
        val outline = shape.createOutline(size, layoutDirection, scope)

        shadow.apply { setOutline(outline); record { } }
        clip?.apply { rewind(); addOutline(outline) }

        currentShape = shape

        onDrawBehind {
            shadow.apply {
                shadowElevation = elevation.toPx()
                ambientShadowColor = actualAmbientColor
                spotShadowColor = actualSpotColor
            }

            val layer = layerNode
            if (layer != null) {
                layer.color = layerColor
                layer.draw(this)
            } else {
                drawShadow(this)
            }

            currentElevation = elevation
            currentAmbientColor = actualAmbientColor
            currentSpotColor = actualSpotColor
            currentLayerColor = layerColor
        }
    }

    private fun drawShadow(scope: DrawScope) = with(scope) {
        val clip = this@ShadowNode.clip
        if (clip != null) {
            clipPath(clip, ClipOp.Difference) { drawLayer(shadow) }
        } else {
            drawLayer(shadow)
        }
    }
}