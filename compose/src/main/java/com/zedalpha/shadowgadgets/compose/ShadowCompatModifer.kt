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