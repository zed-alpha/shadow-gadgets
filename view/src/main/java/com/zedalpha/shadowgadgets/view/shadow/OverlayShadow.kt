package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.ShadowForge
import com.zedalpha.shadowgadgets.core.ViewShadowColors28
import com.zedalpha.shadowgadgets.view.pathProvider

internal class OverlayShadow(
    private val targetView: View,
    private val drawPlane: DrawPlane,
    private val controller: OverlayController
) : ViewShadow {

    private val clippedShadow = ShadowForge.createClippedShadow(targetView)

    private val provider: ViewOutlineProvider = targetView.outlineProvider

    private val willDraw get() = targetView.isVisible

    fun attachToTarget() {
        val target = targetView
        clippedShadow.pathProvider = PathProvider { path ->
            target.pathProvider?.getPath(target, path)
        }
        target.outlineProvider = OutlineProviderWrapper(provider, clippedShadow)
        target.shadow = this
        show()
    }

    fun detachFromTarget() {
        val target = targetView
        clippedShadow.dispose()
        target.outlineProvider = provider
        target.shadow = null
        hide()
    }

    fun show() {
        drawPlane.addShadow(this)
    }

    fun hide() {
        drawPlane.removeShadow(this)
    }

    override fun notifyDetach() {
        controller.removeShadow(this)
    }

    private var left = 0
    private var top = 0
    private var right = 0
    private var bottom = 0

    fun checkInvalidate(): Boolean {
        val target = targetView
        val shadow = clippedShadow

        if (left != target.left) return true
        if (top != target.top) return true
        if (right != target.right) return true
        if (bottom != target.bottom) return true
        if (shadow.alpha != target.alpha) return true
        if (shadow.cameraDistance != target.cameraDistance) return true
        if (shadow.elevation != target.elevation) return true
        if (shadow.pivotX != target.pivotX) return true
        if (shadow.pivotY != target.pivotY) return true
        if (shadow.rotationX != target.rotationX) return true
        if (shadow.rotationY != target.rotationY) return true
        if (shadow.rotationZ != target.rotation) return true
        if (shadow.scaleX != target.scaleX) return true
        if (shadow.scaleY != target.scaleY) return true
        if (shadow.translationX != target.translationX) return true
        if (shadow.translationY != target.translationY) return true
        if (shadow.translationZ != target.translationZ) return true
        if (Build.VERSION.SDK_INT >= 28) {
            if (shadow.ambientColor != ViewShadowColors28.getAmbientColor(target)) {
                return true
            }
            if (shadow.spotColor != ViewShadowColors28.getSpotColor(target)) {
                return true
            }
        }
        return false
    }

    fun draw(canvas: Canvas) {
        if (willDraw) {
            update()
            val left = left.toFloat()
            val top = top.toFloat()
            canvas.translate(left, top)
            clippedShadow.draw(canvas)
            canvas.translate(-left, -top)
        }
    }

    private fun update() {
        val target = targetView
        val shadow = clippedShadow

        left = target.left
        top = target.top
        right = target.right
        bottom = target.bottom
        shadow.alpha = target.alpha
        shadow.cameraDistance = target.cameraDistance
        shadow.elevation = target.elevation
        shadow.pivotX = target.pivotX
        shadow.pivotY = target.pivotY
        shadow.rotationX = target.rotationX
        shadow.rotationY = target.rotationY
        shadow.rotationZ = target.rotation
        shadow.scaleX = target.scaleX
        shadow.scaleY = target.scaleY
        shadow.translationX = target.translationX
        shadow.translationY = target.translationY
        shadow.translationZ = target.translationZ
        if (Build.VERSION.SDK_INT >= 28) {
            shadow.ambientColor = ViewShadowColors28.getAmbientColor(target)
            shadow.spotColor = ViewShadowColors28.getSpotColor(target)
        }
    }
}