package com.zedalpha.shadowgadgets.view

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import com.zedalpha.shadowgadgets.core.DefaultAmbientShadowAlpha
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.DefaultSpotShadowAlpha
import com.zedalpha.shadowgadgets.core.blendShadowColors
import com.zedalpha.shadowgadgets.core.isNotDefault
import com.zedalpha.shadowgadgets.core.resolveThemeShadowAlphas
import com.zedalpha.shadowgadgets.view.shadow.checkShadow
import com.zedalpha.shadowgadgets.view.shadow.shadow

/**
 * The current color compat value for the receiver View.
 *
 * This features allows shadows to be tinted on API levels 21..27, before the
 * native ambient and spot colors were introduced. It's also possible to force
 * this effect on later API levels with the [forceOutlineShadowColorCompat]
 * property.
 *
 * The compat mechanism works by tinting a regular black shadow with the given
 * color. If this property's value is set to black, the compat mechanism is
 * disabled, and the shadow is left to be drawn normally.
 *
 * The default value is black, so color compat is initially disabled.
 *
 * While this feature is active, the receiver's
 * [ViewOutlineProvider][android.view.ViewOutlineProvider] is wrapped
 * in a custom library implementation. Any user implementations should be set
 * before enabling this feature, or at least before the View attaches to its
 * Window.
 *
 * When `true`, the View's intrinsic shadow is always disabled, even if the
 * replacement cannot be drawn, for whatever reason.
 */
@get:ColorInt
@setparam:ColorInt
public var View.outlineShadowColorCompat: Int
    get() = getTag(R.id.outline_shadow_color_compat) as? Int
        ?: DefaultShadowColorInt
    set(color) {
        if (outlineShadowColorCompat == color) return
        setTag(R.id.outline_shadow_color_compat, color)
        updateColorOutlineShadow()
        shadow?.updateLayer()
    }

/**
 * Determines whether the color compat mechanism should be forced if the current
 * API level is 28 or above.
 *
 * When set to true, on the relevant versions, the receiver View's
 * [outlineAmbientShadowColor][android.view.View.setOutlineAmbientShadowColor]
 * and
 * [outlineSpotShadowColor][android.view.View.setOutlineSpotShadowColor] should
 * not be modified afterward. The native shadow must be pure black for the
 * tint to apply correctly.
 */
public var View.forceOutlineShadowColorCompat: Boolean
    get() = getTag(R.id.force_outline_shadow_color_compat) == true
    set(force) {
        if (forceOutlineShadowColorCompat == force) return
        setTag(R.id.force_outline_shadow_color_compat, force)
        updateColorOutlineShadow()
        shadow?.updateLayer()
    }

/**
 * Helper class that blends the two native shadow colors into a single value
 * appropriate for use with [outlineShadowColorCompat].
 *
 * The ambient and spot colors are blended in proportion to their corresponding
 * alpha values set in the Context's theme.
 *
 * Use of this class is completely optional. Any valid color can be used with
 * the compat functionality.
 */
public class ShadowColorsBlender(private val context: Context) {

    private var ambientAlpha = DefaultAmbientShadowAlpha

    private var spotAlpha = DefaultSpotShadowAlpha

    init {
        setAlphas()
    }

    /**
     * Calculates a color blended from [ambientColor] and [spotColor],
     * proportional to their theme alpha values.
     *
     * The current blending formula gives proper results only if both colors are
     * fully opaque; i.e., only if both ambientColor and spotColor have
     * maximum alpha values.
     */
    @ColorInt
    public fun blend(
        @ColorInt ambientColor: Int,
        @ColorInt spotColor: Int
    ): Int = blendShadowColors(ambientColor, ambientAlpha, spotColor, spotAlpha)

    /**
     * To be called from the corresponding function in the relevant UI component.
     *
     * This is necessary only if configuration changes are already being handled
     * manually, and different app themes have different shadow alpha values.
     */
    @Deprecated(
        "Pointless, as it's not possible to change " +
                "alphas without creating a new Activity instance."
    )
    public fun onConfigurationChanged(): Unit = setAlphas()

    private fun setAlphas() {
        val (ambient, spot) = context.resolveThemeShadowAlphas()
        ambientAlpha = ambient
        spotAlpha = spot
    }
}

private fun View.updateColorOutlineShadow(): Boolean {
    val newValue = outlineShadowColorCompat.isNotDefault &&
            (Build.VERSION.SDK_INT < 28 || forceOutlineShadowColorCompat)
    val updated = colorOutlineShadow != newValue
    colorOutlineShadow = newValue
    return updated
}

internal var View.colorOutlineShadow: Boolean
    get() = getTag(R.id.color_outline_shadow) == true
    private set(value) {
        if (colorOutlineShadow == value) return
        setTag(R.id.color_outline_shadow, value)
        checkShadow()
    }