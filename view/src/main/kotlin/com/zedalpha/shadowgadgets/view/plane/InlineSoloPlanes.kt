package com.zedalpha.shadowgadgets.view.plane

import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.graphics.withMatrix
import com.zedalpha.shadowgadgets.view.internal.BaseDrawable
import com.zedalpha.shadowgadgets.view.internal.OnPreDraw
import com.zedalpha.shadowgadgets.view.internal.ThreadLocalGraphicsTemps
import com.zedalpha.shadowgadgets.view.internal.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.internal.addOnPreDraw
import com.zedalpha.shadowgadgets.view.internal.invalidator
import com.zedalpha.shadowgadgets.view.internal.removeOnPreDraw
import com.zedalpha.shadowgadgets.view.layer.Layer
import com.zedalpha.shadowgadgets.view.layer.SoloLayer
import com.zedalpha.shadowgadgets.view.layer.desiredLayerColor
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.shadow.Shadow
import kotlin.math.roundToInt

internal class ChildSoloPlane(
    proxy: ShadowProxy,
    override val viewGroup: ViewGroup
) : InlineSoloPlane(proxy) {

    private val invalidator = viewGroup.invalidator()

    init {
        invalidator.add(this)
    }

    override fun dispose() {
        super.dispose()
        invalidator.remove(this)
    }

    override fun onDraw(canvas: Canvas, matrix: Matrix) {
        val proxy = proxy
        val target = proxy.target
        matrix.preTranslate(-target.left.toFloat(), -target.top.toFloat())
        canvas.withMatrix(matrix) {
            proxy.layer?.draw(canvas) ?: proxy.shadow.draw(canvas)
        }
    }

    override fun invalidate() = invalidator.invalidate()

    override fun createLayer(): Layer =
        SoloLayer(viewGroup, proxy.shadow::draw, ::invalidate)
}

internal class RootSoloPlane(proxy: ShadowProxy) : InlineSoloPlane(proxy) {

    override val viewGroup: ViewGroup? = null

    override fun onDraw(canvas: Canvas, matrix: Matrix) {
        val layer = proxy.layer
        val shadow = proxy.shadow

        if (layer != null) {
            val layerBounds = layer.bounds
            shadow.translationX -= layerBounds.left.toFloat()
            shadow.translationY -= layerBounds.top.toFloat()
            canvas.withMatrix(matrix) { layer.draw(canvas) }
        } else {
            canvas.withMatrix(matrix) { shadow.draw(canvas) }
        }
    }

    override fun invalidate() = proxy.target.invalidate()

    override fun createLayer(): Layer {
        val target = proxy.target
        return SoloLayer(target, proxy.shadow::draw, ::invalidate) { bounds ->
            // Insets from WindowManager.LayoutParams.setSurfaceInsets().
            val ds = (2 * target.z).roundToInt()
            bounds.inset(-ds, -ds)
        }
    }
}

internal abstract class InlineSoloPlane(protected val proxy: ShadowProxy) :
    Plane {

    private val drawable =
        object : BaseDrawable() {

            private val matrix = ThreadLocalGraphicsTemps.matrix

            override fun draw(canvas: Canvas) {
                if (!proxy.updateAndConfirmDraw()) return

                val matrix = matrix
                proxy.shadow.getInverseMatrix(matrix)

                onDraw(canvas, matrix)
            }
        }

    protected abstract fun onDraw(canvas: Canvas, matrix: Matrix)

    private val checkInvalidate =
        OnPreDraw { if (isInvalid(proxy)) invalidate() }

    init {
        (viewGroup ?: proxy.target).addOnPreDraw(checkInvalidate)
        proxy.target.overlay.add(drawable)
    }

    @CallSuper
    protected open fun dispose() {
        (viewGroup ?: proxy.target).removeOnPreDraw(checkInvalidate)
        proxy.target.overlay.remove(drawable)
        proxy.layer?.let { proxy.layer = null; it.dispose() }
    }

    override fun addProxy(proxy: ShadowProxy) = updateLayer(proxy)

    override fun updateLayer(proxy: ShadowProxy) {
        val color = proxy.desiredLayerColor
        if (proxy.layer?.color == color) return

        if (color == null) {
            proxy.layer?.let { proxy.layer = null; it.dispose() }
        } else {
            val layer = proxy.layer ?: createLayer().also { proxy.layer = it }
            layer.color = color
        }
    }

    protected abstract fun createLayer(): Layer

    override fun removeProxy(proxy: ShadowProxy) {
        if (this.proxy == proxy) dispose()
    }

    final override fun Shadow.doesNotMatch(target: View): Boolean {
        if (this.translationZ != target.translationZ) return true
        if (this.alpha != target.alpha) return true
        if (this.elevation != target.elevation) return true
        if (Build.VERSION.SDK_INT >= 28) {
            val ambient = ViewShadowColorsHelper.getAmbientColor(target)
            if (this.ambientColor != ambient) return true
            val spot = ViewShadowColorsHelper.getSpotColor(target)
            if (this.spotColor != spot) return true
        }
        if (this.cameraDistance != target.cameraDistance) return true
        return false
    }
}