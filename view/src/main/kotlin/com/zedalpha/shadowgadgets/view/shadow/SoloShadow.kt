package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.core.graphics.withSave
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.internal.BaseDrawable

internal class SoloShadow(
    targetView: View,
    controller: SoloController,
    private val shadowScope: View?
) : ViewShadow(targetView, controller) {

    private val drawable = object : BaseDrawable() {

        override fun draw(canvas: Canvas) {
            val shadow = coreShadow
            if (!targetView.updateAndCheckDraw(shadow) || !isShown) return

            updateBounds()

            canvas.withSave {
                if (!shadow.hasIdentityMatrix()) {
                    val matrix = tmpMatrix ?: Matrix().also { tmpMatrix = it }
                    shadow.getMatrix(matrix)
                    matrix.invert(matrix)
                    concat(matrix)
                }
                if (shadowScope != null) {
                    translate(bounds.left.toFloat(), bounds.top.toFloat())
                }
                coreLayer?.draw(canvas) ?: shadow.draw(this)
            }
        }

        fun updateBounds() {
            val scope = shadowScope
            val newBounds = tmpRect

            if (scope != null) {
                newBounds.set(0, 0, scope.width, scope.height)
                newBounds.offset(-targetView.left, -targetView.top)
            } else {
                val size = controller.layerSize
                newBounds.set(0, 0, size.width, size.height)

                val location = tmpInts ?: IntArray(2).also { tmpInts = it }
                targetView.getLocationOnScreen(location)
                newBounds.offset(-location[0], -location[1])

                val shadow = coreShadow
                shadow.translationX = location[0].toFloat()
                shadow.translationY = location[1].toFloat()
            }

            bounds = newBounds
        }
    }

    init {
        targetView.overlay.add(drawable)
        initialize()
        drawable.invalidateSelf()
    }

    override fun detachFromTarget() {
        super.detachFromTarget()
        targetView.overlay.remove(drawable)
    }

    fun shouldInvalidate(): Boolean {
        if (!isShown) return false

        val target = targetView
        val shadow = coreShadow

        if (shadow.translationZ != target.translationZ) return true
        if (shadow.elevation != target.elevation) return true
        if (shadow.alpha != target.alpha) return true
        if (Build.VERSION.SDK_INT >= 28) {
            if (shadow.ambientColor !=
                ViewShadowColorsHelper.getAmbientColor(target)
            ) return true
            if (shadow.spotColor !=
                ViewShadowColorsHelper.getSpotColor(target)
            ) return true
        }
        if (shadow.cameraDistance != target.cameraDistance) return true
        return false
    }

    override fun invalidate() = drawable.invalidateSelf()
}

private val tmpRect = Rect()
private var tmpInts: IntArray? = null
private var tmpMatrix: Matrix? = null