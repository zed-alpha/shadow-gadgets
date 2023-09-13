package com.zedalpha.shadowgadgets.compose

import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.platform.inspectable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.internal.baseShadow
import com.zedalpha.shadowgadgets.compose.internal.blend


@OptIn(ExperimentalColorCompat::class)
@Stable
fun Modifier.clippedShadow(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor
) = clippedShadow(
    elevation = elevation,
    shape = shape,
    clip = clip,
    ambientColor = ambientColor,
    spotColor = spotColor,
    colorCompat = DefaultShadowColor,
    forceColorCompat = false
)

@ExperimentalColorCompat
@Stable
fun Modifier.clippedShadow(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor,
    colorCompat: Color? = DefaultShadowColor,
    forceColorCompat: Boolean = false
) = if (elevation > 0.dp || clip) {
    inspectable(
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
        composed {
            val compat = when {
                Build.VERSION.SDK_INT < 28 || forceColorCompat -> {
                    colorCompat ?: blend(ambientColor, spotColor)
                }

                else -> DefaultShadowColor
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
    }
} else {
    this
}