package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow

internal abstract class ViewShadow(protected val targetView: View) {

    protected val provider: ViewOutlineProvider = targetView.outlineProvider

    val isClipped: Boolean = targetView.clipOutlineShadow

    var isShown: Boolean = true

    init {
        @Suppress("LeakingThis")
        targetView.shadow = this
    }

    protected fun wrapOutlineProvider(setOutline: (Outline) -> Unit) {
        targetView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                provider.getOutline(view, outline)
                setOutline.invoke(outline)
                outline.alpha = 0F
            }
        }
    }

    @CallSuper
    open fun detachFromTarget() {
        targetView.outlineProvider = provider
        targetView.shadow = null
    }

    abstract fun updateColorCompat(color: Int)

    abstract fun invalidate()

    protected fun View.updateAndCheckDraw(shadow: Shadow): Boolean {
        shadow.setPosition(left, top, right, bottom)
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
        return isVisible && elevation > 0F
    }
}

internal inline var View.shadow: ViewShadow?
    get() = getTag(R.id.shadow) as? ViewShadow
    private set(value) = setTag(R.id.shadow, value)