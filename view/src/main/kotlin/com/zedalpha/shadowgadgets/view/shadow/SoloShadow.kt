package com.zedalpha.shadowgadgets.view.shadow

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Point
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable
import com.zedalpha.shadowgadgets.view.forceShadowLayer
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.pathProvider

internal class SoloShadow(
    targetView: View,
    private val shadowScope: ViewGroup?
) : ViewShadow(targetView) {

    private val drawable = object : ShadowDrawable(targetView, isClipped) {

        override fun draw(canvas: Canvas) {
            if (!targetView.updateAndCheckDraw(coreShadow) || !isShown) return

            updateBounds()

            canvas.save()
            if (!hasIdentityMatrix()) {
                val matrix = tmpMatrix ?: Matrix().also { tmpMatrix = it }
                getMatrix(matrix)
                matrix.invert(matrix)
                canvas.concat(matrix)
            }
            if (shadowScope != null) {
                canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
            }
            super.draw(canvas)
            canvas.restore()
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

                translationX = ints[0].toFloat()
                translationY = ints[1].toFloat()
            }

            bounds = newBounds
        }

        private val tmpBounds = Rect()
        private var tmpInts: IntArray? = null
        private var windowManager: WindowManager? = null
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (isShown && checkInvalidate()) drawable.invalidateSelf()
        true
    }

    private fun checkInvalidate(): Boolean {
        if (!isShown) return false

        val target = targetView
        val shadow = drawable.coreShadow

        if (shadow.alpha != target.alpha) return true
        if (shadow.cameraDistance != target.cameraDistance) return true
        if (shadow.elevation != target.elevation) return true
        if (shadow.translationZ != target.translationZ) return true
        if (Build.VERSION.SDK_INT >= 28) {
            if (shadow.ambientColor !=
                ViewShadowColorsHelper.getAmbientColor(target)
            ) return true
            if (shadow.spotColor !=
                ViewShadowColorsHelper.getSpotColor(target)
            ) return true
        }
        return false
    }

    private var viewTreeObserver: ViewTreeObserver? = null

    init {
        drawable.updateBounds()
        targetView.overlay.add(drawable)

        // Must set this before wrapping the OutlineProvider.
        targetView.pathProvider?.let { pathProvider ->
            drawable.setClipPathProvider { path ->
                pathProvider.getPath(targetView, path)
            }
        }
        wrapOutlineProvider(drawable::setOutline)

        viewTreeObserver = targetView.viewTreeObserver.apply {
            if (isAlive) addOnPreDrawListener(preDrawListener)
        }

        if (targetView.colorOutlineShadow) {
            drawable.colorCompat = targetView.outlineShadowColorCompat
        }
        drawable.forceLayer = targetView.forceShadowLayer
        drawable.invalidateSelf()
    }

    override fun detachFromTarget() {
        super.detachFromTarget()

        targetView.overlay.remove(drawable)

        viewTreeObserver?.run {
            if (isAlive) removeOnPreDrawListener(preDrawListener)
            viewTreeObserver = null
        }

        drawable.setClipPathProvider(null)
        drawable.dispose()
    }

    override fun updateColorCompat(color: Int) =
        drawable.run {
            if (colorCompat == color) return
            colorCompat = color
            invalidateSelf()
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