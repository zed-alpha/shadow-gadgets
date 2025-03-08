package com.zedalpha.shadowgadgets.core

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.DoNotInline
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi

public const val DefaultShadowColorInt: Int = Color.BLACK

public const val DefaultAmbientShadowAlpha: Float = 0.039F

public const val DefaultSpotShadowAlpha: Float = 0.19F

@Suppress("KotlinConstantConditions")  // TODO
public val Int.isDefault: Boolean get() = this == DefaultShadowColorInt

@Suppress("KotlinConstantConditions")  // TODO
public val Int.isNotDefault: Boolean get() = this != DefaultShadowColorInt

@RequiresApi(28)
public object ViewShadowColorsHelper {

    @DoNotInline
    public fun getAmbientColor(view: View): Int = view.outlineAmbientShadowColor

    @DoNotInline
    public fun setAmbientColor(view: View, @ColorInt color: Int) {
        view.outlineAmbientShadowColor = color
    }

    @DoNotInline
    public fun getSpotColor(view: View): Int = view.outlineSpotShadowColor

    @DoNotInline
    public fun setSpotColor(view: View, @ColorInt color: Int) {
        view.outlineSpotShadowColor = color
    }
}

public fun Context.resolveThemeShadowAlphas(): Pair<Float, Float> {
    val array = obtainStyledAttributes(R.styleable.Lighting)
    val ambientAlpha = array.getFloat(
        R.styleable.Lighting_android_ambientShadowAlpha,
        DefaultAmbientShadowAlpha
    )
    val spotAlpha = array.getFloat(
        R.styleable.Lighting_android_spotShadowAlpha,
        DefaultSpotShadowAlpha
    )
    array.recycle()
    return ambientAlpha.coerceIn(0F..1F) to spotAlpha.coerceIn(0F..1F)
}

public fun blendShadowColors(
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