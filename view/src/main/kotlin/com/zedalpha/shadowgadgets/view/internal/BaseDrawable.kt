package com.zedalpha.shadowgadgets.view.internal

import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable

internal abstract class BaseDrawable : Drawable() {

    fun superSetBounds(left: Int, top: Int, right: Int, bottom: Int) =
        super.setBounds(left, top, right, bottom)

    @Deprecated("Deprecated in Drawable")
    final override fun getOpacity() = PixelFormat.TRANSLUCENT

    @Deprecated("Library stop")
    final override fun setAlpha(alpha: Int) = Unit

    @Deprecated("Library stop")
    final override fun setColorFilter(filter: ColorFilter?) = Unit

    @Deprecated("Library stop")
    final override fun getOutline(outline: Outline) = Unit

    @Deprecated("Library stop")
    final override fun getPadding(padding: Rect): Boolean = false

    @Deprecated("Library stop")
    final override fun setBounds(bounds: Rect) = Unit

    @Deprecated("Library stop")
    final override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) =
        Unit

    @Deprecated("Library stop")
    final override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
        false
}