package com.zedalpha.shadowgadgets.compose.internal

import android.os.Build
import androidx.annotation.CallSuper
import androidx.compose.ui.draw.CacheDrawModifierNode
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DrawResult
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.layer.setOutline
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.node.DelegatingNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.requireDensity
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.node.requireView
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.toIntSize

internal abstract class ShadowNode(protected var shape: Shape) :
    DelegatingNode() {

    protected abstract val isClipped: Boolean

    private val drawNode = delegate(CacheDrawModifierNode(::cacheDraw))

    final override val shouldAutoInvalidate: Boolean get() = false

    protected lateinit var shadow: GraphicsLayer
        private set

    protected var layerNode: LayerNode? = null
        private set

    @CallSuper
    override fun onAttach() {
        shadow = requireGraphicsContext().createGraphicsLayer()
        checkDensity()
        invalidateDrawCache()
    }

    final override fun onDetach() =
        requireGraphicsContext().releaseGraphicsLayer(shadow)

    protected abstract val shadowScope: WorkingShadowScope

    private var colorBlender: ColorBlender? = null

    protected fun updateShadow(): Boolean {
        val shadow = this.shadow
        val scope = this.shadowScope

        var changed = false

        if (shadow.shadowElevation != scope.elevation) {
            shadow.shadowElevation = scope.elevation
            changed = true
        }

        val noTint = Build.VERSION.SDK_INT >= 28 && !scope.forceColorCompat

        val nextAmbient: Color
        val nextSpot: Color
        if (noTint) {
            nextAmbient = scope.ambientColor
            nextSpot = scope.spotColor
        } else {
            nextAmbient = DefaultShadowColor
            nextSpot = DefaultShadowColor
        }
        if (shadow.ambientShadowColor != nextAmbient) {
            shadow.ambientShadowColor = nextAmbient
            changed = true
        }
        if (shadow.spotShadowColor != nextSpot) {
            shadow.spotShadowColor = nextSpot
            changed = true
        }

        val layerColor = this.layerNode?.color ?: Color.Unspecified
        val nextColor =
            if (noTint || scope.isColorCompatDefault()) {
                if (isClipped && ClipRequiresLayer) DefaultShadowColor
                else Color.Unspecified
            } else {
                scope.colorCompat.takeOrElse {
                    val blender = colorBlender
                        ?: ColorBlender(requireView().context)
                            .also { colorBlender = it }
                    blender.blend(scope.ambientColor, scope.spotColor)
                }
            }
        if (layerColor != nextColor) {
            if (nextColor.isSpecified) {
                val layer = layerNode
                    ?: LayerNode(this).also { delegate(it); layerNode = it }
                layer.color = nextColor
            } else {
                layerNode?.let { undelegate(it); layerNode = null }
            }
            changed = true
        }

        return changed
    }

    protected fun cacheDraw(scope: CacheDrawScope): DrawResult {
        val outline =
            shape.createOutline(scope.size, scope.layoutDirection, scope)
        onSetOutline(outline)

        val shadow = this.shadow
        shadow.setOutline(outline)
        shadow.record(scope, scope.layoutDirection, scope.size.toIntSize()) {}

        return scope.onDrawBehind { onDraw(this) }
    }

    protected open fun onSetOutline(outline: Outline) {}

    @CallSuper
    protected open fun onDraw(scope: DrawScope) =
        layerNode?.draw(scope) ?: drawShadow(scope)

    @CallSuper
    open fun drawShadow(scope: DrawScope) = scope.drawLayer(shadow)

    final override fun onDensityChange() {
        if (isAttached) updateDensity(shadowScope, requireDensity())
    }

    private fun updateDensity(working: MutableDensity, next: Density) {
        working.density = next.density
        working.fontScale = next.fontScale
        onDensityInvalidated()
        invalidateDrawCache()
    }

    protected open fun onDensityInvalidated() {}

    private fun checkDensity() {
        val working = shadowScope
        val next = requireDensity()
        if (working.density != next.density ||
            working.fontScale != next.fontScale
        ) {
            updateDensity(working, next)
        }
    }

    fun invalidateDraw() = drawNode.invalidateDraw()

    protected fun invalidateDrawCache() = drawNode.invalidateDrawCache()

    val shadowElevation: Float get() = shadow.shadowElevation
}