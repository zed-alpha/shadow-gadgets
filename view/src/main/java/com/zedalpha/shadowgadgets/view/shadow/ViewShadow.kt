package com.zedalpha.shadowgadgets.view.shadow

import android.os.Build
import android.view.View
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.R


internal interface ViewShadow {

    var isShown: Boolean

    fun detachFromTarget()

    fun checkRecreate(): Boolean

    fun updateColorCompat(color: Int)

    fun invalidate()
}

internal var View.shadow: ViewShadow?
    get() = getTag(R.id.shadow) as? ViewShadow
    set(value) = setTag(R.id.shadow, value)

internal fun View.checkDrawAndUpdate(shadow: Shadow): Boolean {
    if (!(isVisible && elevation > 0F)) return false

    shadow.setPosition(
        left,
        top,
        right,
        bottom
    )
    shadow.alpha = alpha
    shadow.cameraDistance = cameraDistance
    shadow.elevation = elevation
    shadow.pivotX = pivotX
    shadow.pivotY = pivotY
    shadow.rotationX = rotationX
    shadow.rotationY = rotationY
    shadow.rotationZ = rotation
    shadow.scaleX = scaleX
    shadow.scaleY = scaleY
    shadow.translationX = translationX
    shadow.translationY = translationY
    shadow.translationZ = translationZ
    if (Build.VERSION.SDK_INT >= 28) {
        shadow.ambientColor = ViewShadowColorsHelper.getAmbientColor(this)
        shadow.spotColor = ViewShadowColorsHelper.getSpotColor(this)
    }
    return true
}