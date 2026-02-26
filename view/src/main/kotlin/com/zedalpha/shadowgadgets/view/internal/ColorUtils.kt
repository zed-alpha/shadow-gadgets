package com.zedalpha.shadowgadgets.view.internal

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi

internal const val DefaultShadowColor: Int = Color.BLACK

internal inline val Int.isDefault: Boolean get() = this == Color.BLACK

internal inline val Int.isNotDefault: Boolean get() = this != Color.BLACK

internal inline val Int.isTint: Boolean
    get() = this != Color.BLACK && this != Color.TRANSPARENT

internal inline val Int.isNotTint: Boolean
    get() = this == Color.BLACK || this == Color.TRANSPARENT

@RequiresApi(28)
internal object ViewShadowColorsHelper {

    @DoNotInline
    fun getAmbientColor(view: View): Int = view.outlineAmbientShadowColor

    @DoNotInline
    fun setAmbientColor(view: View, @ColorInt color: Int) {
        view.outlineAmbientShadowColor = color
    }

    @DoNotInline
    fun getSpotColor(view: View): Int = view.outlineSpotShadowColor

    @DoNotInline
    fun setSpotColor(view: View, @ColorInt color: Int) {
        view.outlineSpotShadowColor = color
    }
}