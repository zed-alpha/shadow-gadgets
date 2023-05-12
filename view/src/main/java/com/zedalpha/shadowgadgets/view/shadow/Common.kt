package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewOutlineProvider
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.view.R


internal sealed interface ViewShadow {
    fun notifyDetach()
}

internal var View.shadow: ViewShadow?
    get() = getTag(R.id.shadow) as? ViewShadow
    set(value) = setTag(R.id.shadow, value)

internal abstract class BaseDrawable : Drawable() {

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(filter: ColorFilter?) {}
}

internal class OutlineProviderWrapper(
    private val wrapped: ViewOutlineProvider,
    private val shadow: Shadow
) : ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        shadow.setOutline(outline)
        outline.alpha = 0.0F
    }
}