package com.zedalpha.shadowgadgets.view

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import com.zedalpha.shadowgadgets.core.DefaultAmbientShadowAlpha
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.DefaultSpotShadowAlpha
import com.zedalpha.shadowgadgets.core.blendShadowColors
import com.zedalpha.shadowgadgets.core.resolveThemeShadowAlphas
import com.zedalpha.shadowgadgets.view.shadow.notifyPropertyChanged
import com.zedalpha.shadowgadgets.view.shadow.shadow


@get:ColorInt
@setparam:ColorInt
var View.outlineShadowColorCompat: Int
    get() = getTag(R.id.outline_shadow_color_compat) as? Int
        ?: DefaultShadowColorInt
    set(color) {
        if (outlineShadowColorCompat == color) return
        setTag(R.id.outline_shadow_color_compat, color)
        updateColorOutlineShadow()
        shadow?.updateColorCompat(color)
    }

var View.forceOutlineShadowColorCompat: Boolean
    get() = getTag(R.id.force_outline_shadow_color_compat) == true
    set(force) {
        if (forceOutlineShadowColorCompat == force) return
        setTag(R.id.force_outline_shadow_color_compat, force)
        updateColorOutlineShadow()
        shadow?.invalidate()
    }

var View.forceShadowLayer: Boolean
    get() = getTag(R.id.force_shadow_layer) == true
    set(value) = setTag(R.id.force_shadow_layer, value)

class ShadowColorsBlender(private val context: Context) {

    private var ambientAlpha = DefaultAmbientShadowAlpha

    private var spotAlpha = DefaultSpotShadowAlpha

    init {
        onConfigurationChanged()
    }

    fun onConfigurationChanged() {
        val (ambient, spot) = resolveThemeShadowAlphas(context)
        ambientAlpha = ambient
        spotAlpha = spot
    }

    @ColorInt
    fun blend(
        @ColorInt ambientColor: Int,
        @ColorInt spotColor: Int
    ) = blendShadowColors(
        ambientColor,
        ambientAlpha,
        spotColor,
        spotAlpha
    )
}

private fun View.updateColorOutlineShadow() {
    colorOutlineShadow = outlineShadowColorCompat != DefaultShadowColorInt &&
            (Build.VERSION.SDK_INT < 28 || forceOutlineShadowColorCompat)
}

internal var View.colorOutlineShadow: Boolean
    get() = getTag(R.id.color_outline_shadow) == true
    private set(value) {
        if (colorOutlineShadow == value) return
        setTag(R.id.color_outline_shadow, value)
        notifyPropertyChanged()
    }