package com.zedalpha.shadowgadgets.view.internal

import android.graphics.BlendMode
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.Drawable

internal abstract class BaseDrawable : Drawable() {

    fun superSetBounds(left: Int, top: Int, right: Int, bottom: Int) =
        super.setBounds(left, top, right, bottom)

    @Deprecated("Library stop")
    final override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) =
        Unit

    @Deprecated("Library stop")
    final override fun setBounds(bounds: Rect) = Unit

    @Deprecated("Library stop")
    final override fun setVisible(visible: Boolean, restart: Boolean): Boolean =
        false

    @Deprecated("Library stop")
    @Suppress("OVERRIDE_DEPRECATION")
    final override fun getOpacity() = PixelFormat.TRANSLUCENT

    @Deprecated("Library stop")
    final override fun setAlpha(alpha: Int) = Unit

    @Deprecated("Library stop")
    final override fun setColorFilter(filter: ColorFilter?) = Unit

    @Deprecated("Library stop")
    @Suppress("OVERRIDE_DEPRECATION")
    final override fun setColorFilter(color: Int, mode: PorterDuff.Mode) = Unit

    @Deprecated("Library stop")
    final override fun getIntrinsicWidth(): Int = 0

    @Deprecated("Library stop")
    final override fun getIntrinsicHeight(): Int = 0

    @Deprecated("Library stop")
    final override fun getMinimumWidth(): Int = 0

    @Deprecated("Library stop")
    final override fun getMinimumHeight(): Int = 0

    @Deprecated("Library stop")
    final override fun getOutline(outline: Outline) = outline.setEmpty()

    @Deprecated("Library stop")
    final override fun setTint(tintColor: Int) = Unit

    @Deprecated("Library stop")
    final override fun setTintBlendMode(blendMode: BlendMode?) = Unit

    @Deprecated("Library stop")
    final override fun setTintMode(tintMode: PorterDuff.Mode?) = Unit

    @Deprecated("Library stop")
    final override fun setState(stateSet: IntArray): Boolean = false
}