package com.zedalpha.shadowgadgets.view.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View

internal abstract class BaseView(context: Context) : View(context) {

    @Deprecated("DO NOT USE!")
    final override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) =
        setMeasuredDimension(0, 0)


    @Deprecated("DO NOT USE!")
    final override fun forceLayout() = Unit

    @Deprecated("DO NOT USE!")
    @SuppressLint("MissingSuperCall")
    final override fun requestLayout() = Unit

    @Deprecated("DO NOT USE!")
    final override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) =
        Unit


    fun superInvalidate() = super.invalidate()

    @Deprecated("DO NOT USE!")
    final override fun invalidate() = Unit

    @Deprecated("DO NOT USE!")
    final override fun invalidate(dirty: Rect?) = Unit

    @Deprecated("DO NOT USE!")
    final override fun invalidate(l: Int, t: Int, r: Int, b: Int) = Unit

    @Suppress("unused")
    private fun damageInParent() = Unit

    fun superInvalidateOutline() = super.invalidateOutline()

    @Deprecated("DO NOT USE!")
    final override fun invalidateOutline() = Unit

    @Deprecated("DO NOT USE!")
    final override fun invalidateDrawable(drawable: Drawable) = Unit


    @Deprecated("DO NOT USE!")
    final override fun hasFocus(): Boolean = false

    @Deprecated("DO NOT USE!")
    final override fun hasFocusable(): Boolean = false

    @Deprecated("DO NOT USE!")
    final override fun hasExplicitFocusable(): Boolean = false
}