package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.roundToIntSize

internal abstract class ClippedDropShadowNode(protected var shape: Shape) :
    DelegatingNode(), CompositionLocalConsumerModifierNode {

    private val drawNode = delegate(CacheDrawModifierNode(::cacheDraw))

    final override val shouldAutoInvalidate: Boolean get() = false

    final override fun onAttach() = checkDensity()

    private var currentPainter: Painter? = null

    private val clip = Path()

    private var isClipInvalidated = false

    private var currentSize = Size.Unspecified

    protected abstract fun createPainter(scope: CacheDrawScope): Painter

    private fun cacheDraw(scope: CacheDrawScope): DrawResult {
        val painter =
            currentPainter ?: createPainter(scope).also { currentPainter = it }

        val clip = this.clip
        if (isClipInvalidated || currentSize != scope.size) {
            val outline =
                shape.createOutline(scope.size, scope.layoutDirection, scope)
            clip.rewind()
            clip.addOutline(outline)

            isClipInvalidated = false
            currentSize = scope.size
        }

        return if (ClipRequiresLayer) {
            layerDraw(scope, painter, clip)
        } else {
            scope.onDrawBehind {
                drawShadow(scope, painter, clip)
            }
        }
    }

    protected abstract val shadowMargin: Float

    private fun layerDraw(
        scope: CacheDrawScope,
        painter: Painter,
        clip: Path
    ): DrawResult {
        val layer = scope.obtainGraphicsLayer()
        layer.compositingStrategy =
            if (ClipRequiresOffscreenLayer) {
                CompositingStrategy.Offscreen
            } else {
                CompositingStrategy.ModulateAlpha
            }

        // Sizing is from androidx.compose.ui.graphics.shadow.DropShadowPainter.
        val margin = shadowMargin // = radius + spread

        val size = scope.size
        val outset = 2 * margin
        val layerSize = Size(size.width + outset, size.height + outset)

        layer.record(scope, scope.layoutDirection, layerSize.roundToIntSize()) {
            translate(margin, margin) {
                drawShadow(scope, painter, clip)
            }
        }

        return scope.onDrawBehind {
            translate(-margin, -margin) {
                drawLayer(layer)
            }
        }
    }

    private fun DrawScope.drawShadow(
        scope: CacheDrawScope,
        painter: Painter,
        clip: Path
    ) =
        clipPath(clip, ClipOp.Difference) {
            painter.run { draw(scope.size) }
        }

    final override fun onLayoutDirectionChange() {
        invalidateClip()
        invalidateDrawCache()
    }

    protected abstract val density: MutableDensity

    final override fun onDensityChange() {
        if (isAttached) updateDensity(density, requireDensity())
    }

    private fun updateDensity(density: MutableDensity, next: Density) {
        density.density = next.density
        density.fontScale = next.fontScale
        invalidateClip()
        invalidatePainter()
        invalidateDrawCache()
    }

    private fun checkDensity() {
        val density = this.density
        val next = requireDensity()
        if (density.density != next.density ||
            density.fontScale != next.fontScale
        ) {
            updateDensity(density, next)
        }
    }

    protected fun invalidateClip() {
        isClipInvalidated = true
    }

    protected fun invalidatePainter() {
        currentPainter = null
    }

    protected fun invalidateDrawCache() = drawNode.invalidateDrawCache()
}