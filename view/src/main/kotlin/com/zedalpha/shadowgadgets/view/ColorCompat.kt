package com.zedalpha.shadowgadgets.view

import android.content.Context
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.blendShadowColors
import com.zedalpha.shadowgadgets.view.internal.isNotDefault
import com.zedalpha.shadowgadgets.view.internal.resolveThemeShadowAlphas
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy
import com.zedalpha.shadowgadgets.view.proxy.updatePlane
import com.zedalpha.shadowgadgets.view.proxy.updateProxy

/**
 * The current color compat value for the receiver View.
 *
 * This features allows shadows to be tinted on API levels 21..27, before the
 * native ambient and spot colors were introduced. It's also possible to force
 * this effect on later API levels with the [forceOutlineShadowColorCompat]
 * property.
 *
 * The compat mechanism works by tinting a regular black shadow with the given
 * color. While this property's value is set to black, the compat mechanism is
 * disabled, and the native shadow is left to draw normally.
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
        by viewTag(R.id.outline_shadow_color_compat, DefaultShadowColor) {
            this.updateTint()
        }

/**
 * Determines whether the color compat mechanism should be forced if the current
 * API level is 28 or above.
 *
 * When set to `true`, on the relevant versions, the receiver View's
 * [outlineAmbientShadowColor][View.setOutlineAmbientShadowColor]
 * and
 * [outlineSpotShadowColor][View.setOutlineSpotShadowColor] should
 * not be modified afterward. The native shadow must be pure black for the
 * tint to apply correctly.
 */
public var View.forceOutlineShadowColorCompat: Boolean
        by viewTag(R.id.force_outline_shadow_color_compat, false) {
            this.updateTint()
        }

private fun View.updateTint() {
    this.tintOutlineShadow =
        (Build.VERSION.SDK_INT < 28 || this.forceOutlineShadowColorCompat) &&
                this.outlineShadowColorCompat.isNotDefault

    val proxy = this.shadowProxy ?: return

    // If the Plane changes, the Layer has been handled.
    if (proxy.updatePlane()) return

    proxy.updateLayer()
}

internal var View.tintOutlineShadow: Boolean
        by viewTag(R.id.tint_outline_shadow, false) { this.updateProxy() }
    private set

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

    private var ambientAlpha = 0F
    private var spotAlpha = 0F

    init {
        setAlphas()
    }

    private fun setAlphas() {
        val (ambient, spot) = context.resolveThemeShadowAlphas()
        ambientAlpha = ambient
        spotAlpha = spot
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
    ): Int =
        blendShadowColors(ambientColor, ambientAlpha, spotColor, spotAlpha)

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
}