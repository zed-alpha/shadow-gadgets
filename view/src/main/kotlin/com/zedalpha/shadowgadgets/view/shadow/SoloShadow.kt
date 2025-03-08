package com.zedalpha.shadowgadgets.view.shadow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.core.graphics.withSave
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.internal.BaseDrawable
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat

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

        private var tmpMatrix: Matrix? = null

        fun updateBounds() {
            val scope = shadowScope
            val newBounds = tmpBounds

            if (scope != null) {
                newBounds.set(0, 0, scope.width, scope.height)
                newBounds.offset(-targetView.left, -targetView.top)
            } else {
                val ints = tmpInts ?: IntArray(2).also { tmpInts = it }

                val manager = windowManager
                    ?: targetView.context.windowManager
                        .also { windowManager = it }
                manager.getScreenSize(ints)
                newBounds.set(0, 0, ints[0], ints[1])

                targetView.getLocationOnScreen(ints)
                newBounds.offset(-ints[0], -ints[1])

                val shadow = coreShadow
                shadow.translationX = ints[0].toFloat()
                shadow.translationY = ints[1].toFloat()
            }

            bounds = newBounds
        }

        private val tmpBounds = Rect()
        private var tmpInts: IntArray? = null
        private var windowManager: WindowManager? = null
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (isShown && shouldInvalidate()) drawable.invalidateSelf()
        true
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

    private var viewTreeObserver: ViewTreeObserver? = null

    init {
        drawable.updateBounds()
        targetView.overlay.add(drawable)

        wrapOutlineProvider(coreShadow::setOutline)
        updateColorCompat(targetView.outlineShadowColorCompat)

        viewTreeObserver = targetView.viewTreeObserver.apply {
            if (isAlive) addOnPreDrawListener(preDrawListener)
        }

        drawable.invalidateSelf()
    }

    override fun detachFromTarget() {
        super.detachFromTarget()

        targetView.overlay.remove(drawable)

        viewTreeObserver?.run {
            if (isAlive) removeOnPreDrawListener(preDrawListener)
            viewTreeObserver = null
        }
    }

    override fun invalidate() = drawable.invalidateSelf()
}

private inline val Context.windowManager: WindowManager
    get() = getSystemService(Context.WINDOW_SERVICE) as WindowManager

private fun WindowManager.getScreenSize(outSize: IntArray) =
    if (Build.VERSION.SDK_INT >= 30) {
        val bounds = currentWindowMetrics.bounds
        outSize[0] = bounds.width()
        outSize[1] = bounds.height()
    } else {
        val point = tmpPoint ?: Point().also { tmpPoint = it }
        @Suppress("DEPRECATION")
        defaultDisplay.getSize(point)
        outSize[0] = point.x
        outSize[1] = point.y
    }

private var tmpPoint: Point? = null