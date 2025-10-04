package com.zedalpha.shadowgadgets.view.plane

import android.os.Build
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.internal.Group
import com.zedalpha.shadowgadgets.view.internal.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.shadow.Shadow

internal abstract class GroupPlane(override val viewGroup: ViewGroup) : Plane {

    protected fun Group<ShadowProxy>.isInvalid(): Boolean =
        this.has { proxy -> isInvalid(proxy) }

    final override fun doNotMatch(shadow: Shadow, target: View): Boolean {
        if (shadow.top != target.top) return true
        if (shadow.left != target.left) return true
        if (shadow.right != target.right) return true
        if (shadow.bottom != target.bottom) return true
        if (shadow.translationZ != target.translationZ) return true
        if (shadow.translationX != target.translationX) return true
        if (shadow.translationY != target.translationY) return true
        if (shadow.alpha != target.alpha) return true
        if (shadow.scaleX != target.scaleX) return true
        if (shadow.scaleY != target.scaleY) return true
        if (shadow.rotationX != target.rotationX) return true
        if (shadow.rotationY != target.rotationY) return true
        if (shadow.rotationZ != target.rotation) return true
        if (shadow.elevation != target.elevation) return true
        if (shadow.pivotX != target.pivotX) return true
        if (shadow.pivotY != target.pivotY) return true
        if (Build.VERSION.SDK_INT >= 28) {
            val ambient = ViewShadowColorsHelper.getAmbientColor(target)
            if (shadow.ambientColor != ambient) return true
            val spot = ViewShadowColorsHelper.getSpotColor(target)
            if (shadow.spotColor != spot) return true
        }
        if (shadow.cameraDistance != target.cameraDistance) return true
        return false
    }
}