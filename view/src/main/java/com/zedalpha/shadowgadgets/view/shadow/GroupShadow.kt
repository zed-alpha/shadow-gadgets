package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.requiresColor

internal class GroupShadow(
    targetView: View,
    private val controller: ShadowController,
    private val plane: ShadowPlane
) : ViewShadow(targetView) {

    init {
        show()
    }

    override fun detachFromTarget() {
        super.detachFromTarget()
        controller.removeShadow(this)
        hide()
    }

    override fun show() {
        plane.showShadow(this)
    }

    override fun hide() {
        plane.hideShadow(this)
    }

    override fun updateFilter(color: Int) {
        colorFilter.color = color
        invalidate()
    }

    override fun invalidate() {
        plane.invalidatePlane()
    }

    fun draw(canvas: Canvas) {
        if (targetView.isVisible) {
            update()
            val left = left.toFloat()
            val top = top.toFloat()
            canvas.translate(left, top)
            if (targetView.requiresColor && plane.delegatesFiltering) {
                colorFilter.draw(canvas, shadow)
            } else {
                shadow.draw(canvas)
            }
            canvas.translate(-left, -top)
        }
    }

    private var left = 0
    private var top = 0
    private var right = 0
    private var bottom = 0

    fun checkInvalidate(): Boolean {
        val target = targetView
        val shadow = shadow

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
            if (shadow.ambientColor !=
                ViewShadowColorsHelper.getAmbientColor(target)
            ) return true
            if (shadow.spotColor !=
                ViewShadowColorsHelper.getSpotColor(target)
            ) return true
        }
        return false
    }

    private fun update() {
        val target = targetView
        val shadow = shadow

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
            shadow.ambientColor =
                ViewShadowColorsHelper.getAmbientColor(target)
            shadow.spotColor =
                ViewShadowColorsHelper.getSpotColor(target)
        }
    }
}