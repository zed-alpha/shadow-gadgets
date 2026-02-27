package com.zedalpha.shadowgadgets.view.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View

internal abstract class BaseView(context: Context) : View(context) {

    @Deprecated("Library stop")
    final override fun onMeasure(
        widthMeasureSpec: Int,
        heightMeasureSpec: Int
    ) =
        setMeasuredDimension(0, 0)

    @Deprecated("Library stop")
    final override fun forceLayout() = Unit

    @Deprecated("Library stop")
    @SuppressLint("MissingSuperCall")
    final override fun requestLayout() = Unit

    @Deprecated("Library stop")
    final override fun onLayout(
        changed: Boolean,
        l: Int,
        t: Int,
        r: Int,
        b: Int
    ) =
        Unit

    fun superInvalidate() = super.invalidate()

    @Deprecated("Library stop")
    final override fun invalidate() = Unit

    @Deprecated("Library stop")
    final override fun invalidate(dirty: Rect?) = Unit

    @Deprecated("Library stop")
    final override fun invalidate(l: Int, t: Int, r: Int, b: Int) = Unit

    @Deprecated("Library stop")
    fun damageInParent() = Unit

    fun superInvalidateOutline() = super.invalidateOutline()

    @Deprecated("Library stop")
    final override fun invalidateOutline() = Unit

    @Deprecated("Library stop")
    final override fun invalidateDrawable(drawable: Drawable) = Unit

    @Deprecated("Library stop")
    final override fun hasFocus(): Boolean = false

    @Deprecated("Library stop")
    final override fun hasFocusable(): Boolean = false

    @Deprecated("Library stop")
    final override fun hasExplicitFocusable(): Boolean = false
}