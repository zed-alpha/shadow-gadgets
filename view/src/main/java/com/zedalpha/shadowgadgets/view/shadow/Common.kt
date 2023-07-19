package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R


internal interface ViewShadow {
    fun notifyDetach()
}

internal var View.shadow: ViewShadow?
    get() = getTag(R.id.shadow) as? ViewShadow
    set(value) = setTag(R.id.shadow, value)

internal fun View.recreateShadow() {
    val oldShadow = shadow ?: return
    oldShadow.notifyDetach()
    createShadowForView(this)
    invalidate()
}

internal fun createShadowForView(view: View) {
    val parent = view.parent as? ViewGroup ?: return
    getOrCreateController(parent).addOverlayShadow(view)
}

private fun getOrCreateController(parentView: ViewGroup) =
    parentView.overlayController ?: OverlayController(parentView)

internal abstract class BaseDrawable : Drawable() {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(filter: ColorFilter?) {}
}