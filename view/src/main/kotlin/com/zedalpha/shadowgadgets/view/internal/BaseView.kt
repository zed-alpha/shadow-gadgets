package com.zedalpha.shadowgadgets.view.internal

import android.annotation.SuppressLint
import android.content.Context
import android.view.View

internal open class BaseView(context: Context) : View(context) {

    fun superInvalidate() = super.invalidate()

    fun superInvalidateOutline() = super.invalidateOutline()

    @Deprecated("Library stop", level = DeprecationLevel.ERROR)
    fun damageInParent() = Unit

    @Deprecated("Library stop", level = DeprecationLevel.ERROR)
    final override fun hasFocus(): Boolean = false

    @Deprecated("Library stop", level = DeprecationLevel.ERROR)
    final override fun hasOverlappingRendering(): Boolean = false

    @Deprecated("Library stop", level = DeprecationLevel.ERROR)
    final override fun invalidate() = Unit

    @Deprecated("Library stop", level = DeprecationLevel.ERROR)
    final override fun invalidateOutline() = Unit

    @Deprecated("Library stop", level = DeprecationLevel.ERROR)
    @SuppressLint("MissingSuperCall")
    final override fun requestLayout() = Unit
}