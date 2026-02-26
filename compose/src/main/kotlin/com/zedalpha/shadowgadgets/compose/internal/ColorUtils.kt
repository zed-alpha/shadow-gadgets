package com.zedalpha.shadowgadgets.compose.internal

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.graphics.toArgb

internal inline val Color.isDefault get() = this == DefaultShadowColor

internal inline val Color.isTint: Boolean
    get() = this.isSpecified && this != DefaultShadowColor && this != Transparent

internal fun blendsToDefault(compat: Color, ambient: Color, spot: Color) =
    compat.isUnspecified && ambient.isDefault && spot.isDefault

// Technically, we should account for configuration changes here,
// but since it's not really possible to modify these alphas without
// creating a new Activity instance, I think it's mostly safe to ignore.
internal class ColorBlender(context: Context) {

    private val ambientAlpha: Float
    private val spotAlpha: Float

    init {
        val (ambient, spot) = context.resolveThemeShadowAlphas()
        this.ambientAlpha = ambient
        this.spotAlpha = spot
    }

    private var ambientColor = Color.Unspecified
    private var spotColor = Color.Unspecified
    private var blendedColor = Color.Unspecified

    fun blend(ambientColor: Color, spotColor: Color): Color =
        if (this.ambientColor != ambientColor || this.spotColor != spotColor) {
            this.ambientColor = ambientColor; this.spotColor = spotColor
            val ambient = ambientColor.toArgb()
            val spot = spotColor.toArgb()
            val argb = blendShadowColors(ambient, ambientAlpha, spot, spotAlpha)
            Color(argb).also { blendedColor = it }
        } else {
            blendedColor
        }
}