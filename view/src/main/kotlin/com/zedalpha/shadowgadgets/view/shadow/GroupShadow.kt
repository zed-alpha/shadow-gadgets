package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.os.Build
import android.view.View
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.core.layer.LayerDraw
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat

internal class GroupShadow(
    targetView: View,
    controller: GroupController,
    val plane: DrawPlane
) : ViewShadow(targetView, controller), LayerDraw {

    init {
        plane.addShadow(this)
        wrapOutlineProvider(coreShadow::setOutline)
        updateColorCompat(targetView.outlineShadowColorCompat)

        if (RequiresInvalidateOnToggle) targetView.invalidate()
    }

    override fun detachFromTarget() {
        super.detachFromTarget()
        plane.removeShadow(this)

        if (RequiresInvalidateOnToggle) targetView.invalidate()
    }

    override fun invalidate() = plane.invalidatePlane()

    override fun draw(canvas: Canvas) {
        if (!targetView.updateAndCheckDraw(coreShadow) || !isShown) return
        coreLayer?.draw(canvas) ?: coreShadow.draw(canvas)
    }

    fun shouldInvalidate(): Boolean {
        if (!isShown) return false

        val shadow = coreShadow
        val target = targetView

        if (shadow.translationZ != target.translationZ) return true
        if (shadow.translationY != target.translationY) return true
        if (shadow.translationX != target.translationX) return true
        if (shadow.elevation != target.elevation) return true
        if (shadow.scaleY != target.scaleY) return true
        if (shadow.scaleX != target.scaleX) return true
        if (shadow.rotationZ != target.rotation) return true
        if (shadow.rotationY != target.rotationY) return true
        if (shadow.rotationX != target.rotationX) return true
        if (shadow.pivotY != target.pivotY) return true
        if (shadow.pivotX != target.pivotX) return true
        if (shadow.alpha != target.alpha) return true
        if (shadow.left != target.left) return true
        if (shadow.top != target.top) return true
        if (shadow.right != target.right) return true
        if (shadow.bottom != target.bottom) return true
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
}

internal val RequiresInvalidateOnToggle =
    Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP

internal val View.groupShadow: GroupShadow?
    get() = getTag(R.id.shadow) as? GroupShadow