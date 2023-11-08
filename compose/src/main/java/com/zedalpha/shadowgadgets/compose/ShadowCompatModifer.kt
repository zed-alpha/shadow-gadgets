package com.zedalpha.shadowgadgets.compose

import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.internal.baseShadow
import com.zedalpha.shadowgadgets.compose.internal.blend

/**
 * Creates a shadow replacement that can be tinted with the library's color
 * compat mechanism on API levels before 28, the earliest version to support the
 * native shadow colors.
 *
 * Since this function is essentially an extension of the regular one, please
 * refer to
 * [shadow's documentation](https://developer.android.com/reference/kotlin/androidx/compose/ui/draw/package-summary#(androidx.compose.ui.Modifier).shadow(androidx.compose.ui.unit.Dp,androidx.compose.ui.graphics.Shape,kotlin.Boolean,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color))
 * for details regarding the first five parameters: [elevation], [shape],
 * [clip], [ambientColor], and [spotColor].
 *
 * [colorCompat] takes a Color that's used to flatly tint a plain black shadow,
 * since it's not possible to tint the ambient and spot separately at this
 * level. The parameter is nullable to allow a special behavior: if it's null,
 * the [colorCompat] value is automatically calculated as a blend of the
 * [ambientColor] and [spotColor], mixed in proportion to their current theme
 * alphas. Setting any non-null value disables this behavior.
 *
 * NB: The color blending formula that's currently used gives good results only
 * if the ambient and spot colors are both fully opaque; i.e., only if both
 * have maximum alpha values.
 *
 * [forceColorCompat] is available to force the color compat tinting to be used
 * on API levels 28 and above, for the purposes of testing, consistency, etc.
 */
@ExperimentalColorCompat
@Stable
fun Modifier.shadowCompat(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor,
    colorCompat: Color? = null,
    forceColorCompat: Boolean = false
) = if (elevation > 0.dp || clip) {
    inspectable(
        inspectorInfo = debugInspectorInfo {
            name = "shadowCompat"
            properties["elevation"] = elevation
            properties["shape"] = shape
            properties["clip"] = clip
            properties["ambientColor"] = ambientColor
            properties["spotColor"] = spotColor
            properties["colorCompat"] = colorCompat
            properties["forceColorCompat"] = forceColorCompat
        }
    ) {
        val useNative = Build.VERSION.SDK_INT >= 28 && !forceColorCompat
        if (useNative || colorCompat == DefaultShadowColor) {
            val ambient = if (useNative) ambientColor else DefaultShadowColor
            val spot = if (useNative) spotColor else DefaultShadowColor
            graphicsLayer {
                this.shadowElevation = elevation.toPx()
                this.shape = shape
                this.clip = clip
                this.ambientShadowColor = ambient
                this.spotShadowColor = spot
            }
        } else {
            composed {
                baseShadow(
                    clipped = false,
                    elevation = elevation,
                    shape = shape,
                    clip = clip,
                    ambientColor = ambientColor,
                    spotColor = spotColor,
                    colorCompat = colorCompat ?: blend(ambientColor, spotColor)
                )
            }
        }
    }
} else {
    this
}