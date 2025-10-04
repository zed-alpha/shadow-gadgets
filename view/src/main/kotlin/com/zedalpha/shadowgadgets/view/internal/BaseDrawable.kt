package com.zedalpha.shadowgadgets.view.internal

import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

internal abstract class BaseDrawable : Drawable() {

    fun superSetBounds(left: Int, top: Int, right: Int, bottom: Int) =
        super.setBounds(left, top, right, bottom)

    @Deprecated("DO NOT USE!")
    final override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) =
        Unit

    @Deprecated("DO NOT USE!")
    final override fun setBounds(bounds: Rect) = Unit

    @Deprecated("DO NOT USE!")
    final override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
        false

    @Suppress("OVERRIDE_DEPRECATION")
    final override fun getOpacity() = PixelFormat.TRANSLUCENT
    final override fun setAlpha(alpha: Int) {}
    final override fun setColorFilter(filter: ColorFilter?) {}
}