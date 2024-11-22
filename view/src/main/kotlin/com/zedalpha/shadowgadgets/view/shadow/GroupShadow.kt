package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.os.Build
import android.view.View
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.core.layer.LayerDraw
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.forceShadowLayer
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.pathProvider

internal class GroupShadow(
    targetView: View,
    private val plane: DrawPlane
) : ViewShadow(targetView), LayerDraw {

    private val coreShadow = if (isClipped) {
        ClippedShadow(targetView).also { shadow ->
            val pathProvider = targetView.pathProvider ?: return@also
            shadow.pathProvider = PathProvider { path ->
                pathProvider.getPath(targetView, path)
            }
        }
    } else {
        Shadow(targetView)
    }

    val forceLayer = targetView.forceShadowLayer

    init {
        plane.addShadow(
            this,
            if (targetView.colorOutlineShadow) {
                targetView.outlineShadowColorCompat
            } else {
                DefaultShadowColorInt
            }
        )
        wrapOutlineProvider(coreShadow::setOutline)
    }

    override fun detachFromTarget() {
        super.detachFromTarget()
        plane.removeShadow(this)
        coreShadow.dispose()
    }

    override fun updateColorCompat(color: Int) {
        if (Build.VERSION.SDK_INT >= 28 &&
            targetView.colorOutlineShadow &&
            color != DefaultShadowColorInt
        ) {
            ViewShadowColorsHelper.setAmbientColor(
                targetView,
                DefaultShadowColorInt
            )
            ViewShadowColorsHelper.setSpotColor(
                targetView,
                DefaultShadowColorInt
            )
        }
        plane.updateColor(this, color)
        invalidate()
    }

    override fun invalidate() = plane.invalidatePlane()

    override fun draw(canvas: Canvas) {
        if (!targetView.updateAndCheckDraw(coreShadow) || !isShown) return
        coreShadow.draw(canvas)
    }

    fun checkInvalidate(): Boolean {
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