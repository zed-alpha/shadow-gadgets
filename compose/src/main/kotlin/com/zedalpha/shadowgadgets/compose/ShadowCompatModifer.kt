package com.zedalpha.shadowgadgets.compose

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.isUnspecified
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.internal.ShadowElement
import com.zedalpha.shadowgadgets.compose.internal.ShadowNode
import com.zedalpha.shadowgadgets.compose.internal.isDefault

/**
 * Creates a [shadow] replacement that can be tinted with the library's color
 * compat mechanism on API levels before 28, the earliest version to support the
 * native shadow colors. If the current API level is 28 or above, [shadowCompat]
 * falls back to the normal [shadow], unless [forceColorCompat] is `true`.
 *
 * Refer to [shadow][androidx.compose.ui.draw.shadow]'s docs for details on the
 * first five parameters: [elevation], [shape], [clip], [ambientColor], and
 * [spotColor].
 *
 * [colorCompat] takes a [Color] that's used to tint the shadow on API levels
 * 27 and below. If the passed value is [Color.Black] – the default shadow color
 * – this falls back to the normal [shadow]. If [Color.Unspecified] is passed,
 * the actual value is calculated as a blend of the [ambientColor] and
 * [spotColor], mixed in proportion to their current theme alphas. Setting any
 * other value disables this behavior.
 *
 * NB: The color blending formula that's currently used gives good results only
 * if the ambient and spot colors are both fully opaque; i.e., only if both
 * have maximum alpha values.
 */
@Composable
@Stable
fun Modifier.shadowCompat(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor,
    colorCompat: Color = Color.Unspecified,
    forceColorCompat: Boolean = false
): Modifier =
    if (elevation > 0.dp || clip) {
        if (Build.VERSION.SDK_INT >= 28 && !forceColorCompat ||
            colorCompat.isDefault ||
            colorCompat.isUnspecified &&
            ambientColor.isDefault && spotColor.isDefault
        ) {
            shadow(elevation, shape, clip, ambientColor, spotColor)
        } else {
            val shadow = this then ShadowCompatElement(
                elevation = elevation,
                shape = shape,
                clip = clip,
                ambientColor = ambientColor,
                spotColor = spotColor,
                colorCompat = colorCompat,
                forceColorCompat = forceColorCompat
            )
            if (clip) shadow.clip(shape) else shadow
        }
    } else {
        this
    }

private class ShadowCompatElement(
    elevation: Dp,
    shape: Shape,
    private val clip: Boolean,
    ambientColor: Color,
    spotColor: Color,
    colorCompat: Color,
    forceColorCompat: Boolean
) : ShadowElement(
    elevation,
    shape,
    ambientColor,
    spotColor,
    colorCompat,
    forceColorCompat
) {
    override fun create() =
        ShadowNode(
            false,
            elevation,
            shape,
            ambientColor,
            spotColor,
            colorCompat,
            forceColorCompat
        )

    override fun InspectorInfo.inspectableProperties() {
        name = "shadowCompat"
        properties["elevation"] = elevation
        properties["shape"] = shape
        properties["clip"] = clip
        properties["ambientColor"] = ambientColor
        properties["spotColor"] = spotColor
        properties["colorCompat"] = colorCompat
        properties["forceColorCompat"] = forceColorCompat
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ShadowCompatElement) return false
        if (elevation != other.elevation) return false
        if (shape != other.shape) return false
        if (clip != other.clip) return false
        if (ambientColor != other.ambientColor) return false
        if (spotColor != other.spotColor) return false
        if (colorCompat != other.colorCompat) return false
        if (forceColorCompat != other.forceColorCompat) return false
        return true
    }

    override fun hashCode(): Int {
        var result = elevation.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + clip.hashCode()
        result = 31 * result + ambientColor.hashCode()
        result = 31 * result + spotColor.hashCode()
        result = 31 * result + colorCompat.hashCode()
        result = 31 * result + forceColorCompat.hashCode()
        return result
    }
}