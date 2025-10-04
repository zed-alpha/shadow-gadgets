package com.zedalpha.shadowgadgets.view.internal

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DoNotInline
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.view.R

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

// The rest of this file comprises the color blending utils. They are
// duplicated in :compose rather than involving another separate module.

private const val DefaultAmbientShadowAlpha = 0.039F
private const val DefaultSpotShadowAlpha = 0.19F

internal fun Context.resolveThemeShadowAlphas(): Pair<Float, Float> {
    val array = obtainStyledAttributes(R.styleable.Lighting)
    val ambientAlpha =
        array.getFloat(
            /* index = */ R.styleable.Lighting_android_ambientShadowAlpha,
            /* defValue = */ DefaultAmbientShadowAlpha
        )
    val spotAlpha =
        array.getFloat(
            /* index = */ R.styleable.Lighting_android_spotShadowAlpha,
            /* defValue = */ DefaultSpotShadowAlpha
        )
    array.recycle()
    return ambientAlpha.coerceIn(0F..1F) to spotAlpha.coerceIn(0F..1F)
}

internal fun blendShadowColors(
    @ColorInt
    ambientColor: Int,
    @FloatRange(from = 0.0, to = 1.0)
    ambientAlpha: Float,
    @ColorInt
    spotColor: Int,
    @FloatRange(from = 0.0, to = 1.0)
    spotAlpha: Float
): Int {
    val colorOne = multiplyAlpha(ambientColor, ambientAlpha)
    val colorTwo = multiplyAlpha(spotColor, spotAlpha)
    val alphaOne = Color.alpha(colorOne)
    val alphaTwo = Color.alpha(colorTwo)
    val alphaSum = alphaOne + alphaTwo
    val ratioOne = if (alphaSum != 0) alphaOne.toFloat() / alphaSum else 0F
    val ratioTwo = if (alphaSum != 0) alphaTwo.toFloat() / alphaSum else 0F

    fun blend(one: Int, two: Int) =
        (one * ratioOne + two * ratioTwo + 0.5F).toInt()

    return Color.argb(
        blend(Color.alpha(ambientColor), Color.alpha(spotColor)),
        blend(Color.red(colorOne), Color.red(colorTwo)),
        blend(Color.green(colorOne), Color.green(colorTwo)),
        blend(Color.blue(colorOne), Color.blue(colorTwo))
    )
}

private fun multiplyAlpha(color: Int, alphaF: Float): Int {
    val newAlpha = (Color.alpha(color) * alphaF.coerceIn(0F..1F)).toInt()
    return (newAlpha shl 24) or (color and 0x00ffffff)
}