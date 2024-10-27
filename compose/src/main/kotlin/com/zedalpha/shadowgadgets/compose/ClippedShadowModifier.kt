package com.zedalpha.shadowgadgets.compose

import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.internal.baseShadow
import com.zedalpha.shadowgadgets.compose.internal.blend

/**
 * Creates a clipped replacement for the regular `shadow` Modifier.
 *
 * Refer to [shadow][androidx.compose.ui.draw.shadow]'s docs for parameter
 * details.
 */
@OptIn(ExperimentalColorCompat::class)
@Stable
fun Modifier.clippedShadow(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor
): Modifier = clippedShadow(
    elevation = elevation,
    shape = shape,
    clip = clip,
    ambientColor = ambientColor,
    spotColor = spotColor,
    colorCompat = DefaultShadowColor,
    forceColorCompat = false
)

/**
 * A [clippedShadow] overload that can be tinted with the library's color compat
 * mechanism on API levels before 28, the earliest version to support the native
 * shadow colors.
 *
 * Refer to [shadow][androidx.compose.ui.draw.shadow]'s docs for details on the
 * first five parameters: [elevation], [shape], [clip], [ambientColor], and
 * [spotColor].
 *
 * [colorCompat] takes a [Color] that's used to tint the shadow on API levels
 * 27 and below. If the passed value is [Color.Black] – the default shadow color
 * – no tint is applied. If [Color.Unspecified] is passed, the actual value is
 * calculated as a blend of the [ambientColor] and [spotColor], mixed in
 * proportion to their current theme alphas. Setting any other value disables
 * this behavior.
 *
 * NB: The color blending formula that's currently used gives good results only
 * if the ambient and spot colors are both fully opaque; i.e., only if both
 * have maximum alpha values.
 *
 * [forceColorCompat] is available to force the color compat tinting to be used
 * on API levels 28 and above.
 */
@ExperimentalColorCompat
@Stable
fun Modifier.clippedShadow(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor,
    colorCompat: Color = DefaultShadowColor,
    forceColorCompat: Boolean = false
): Modifier = if (elevation > 0.dp || clip) {
    composed(
        inspectorInfo = debugInspectorInfo {
            name = "clippedShadow"
            properties["elevation"] = elevation
            properties["shape"] = shape
            properties["clip"] = clip
            properties["ambientColor"] = ambientColor
            properties["spotColor"] = spotColor
            properties["colorCompat"] = colorCompat
            properties["forceColorCompat"] = forceColorCompat
        }
    ) {
        val compat = if (Build.VERSION.SDK_INT < 28 || forceColorCompat) {
            colorCompat.takeOrElse { blend(ambientColor, spotColor) }
        } else {
            DefaultShadowColor
        }
        baseShadow(
            clipped = true,
            elevation = elevation,
            shape = shape,
            clip = clip,
            ambientColor = ambientColor,
            spotColor = spotColor,
            colorCompat = compat
        )
    }
} else {
    this
}