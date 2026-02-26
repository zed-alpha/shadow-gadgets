package com.zedalpha.shadowgadgets.view.plane

import android.os.Build
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.internal.Group
import com.zedalpha.shadowgadgets.view.internal.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.shadow.Shadow

internal abstract class GroupPlane(override val viewGroup: ViewGroup) :
    Plane {

    protected fun Group<ShadowProxy>.isInvalid(): Boolean =
        this.has { proxy -> isInvalid(proxy) }

    final override fun Shadow.doesNotMatch(target: View): Boolean {
        if (this.top != target.top) return true
        if (this.left != target.left) return true
        if (this.right != target.right) return true
        if (this.bottom != target.bottom) return true
        if (this.translationZ != target.translationZ) return true
        if (this.translationX != target.translationX) return true
        if (this.translationY != target.translationY) return true
        if (this.alpha != target.alpha) return true
        if (this.scaleX != target.scaleX) return true
        if (this.scaleY != target.scaleY) return true
        if (this.rotationX != target.rotationX) return true
        if (this.rotationY != target.rotationY) return true
        if (this.rotationZ != target.rotation) return true
        if (this.elevation != target.elevation) return true
        if (this.pivotX != target.pivotX) return true
        if (this.pivotY != target.pivotY) return true
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