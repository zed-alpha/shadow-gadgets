package com.zedalpha.shadowgadgets.core

import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi


const val DefaultShadowColorInt = Color.BLACK

@RequiresApi(28)
object ViewShadowColorsHelper {

    @DoNotInline
    fun getAmbientColor(view: View) = view.outlineAmbientShadowColor

    @DoNotInline
    fun setAmbientColor(view: View, @ColorInt color: Int) {
        view.outlineAmbientShadowColor = color
    }

    @DoNotInline
    fun getSpotColor(view: View) = view.outlineSpotShadowColor

    @DoNotInline
    fun setSpotColor(view: View, @ColorInt color: Int) {
        view.outlineSpotShadowColor = color
    }
}