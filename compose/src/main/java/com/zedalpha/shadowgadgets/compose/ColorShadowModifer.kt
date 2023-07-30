package com.zedalpha.shadowgadgets.compose

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.unit.Dp

fun Modifier.colorShadow(
    elevation: Dp,
    shape: Shape = RectangleShape,
    color: Color = DefaultShadowColor
) = if (elevation.value <= 0F) this else composed(
    factory = {
        val localView = LocalView.current
        val shadow = remember(localView) { ComposeShadow(localView, false) }
        DisposableEffect(localView) { onDispose { shadow.dispose() } }
        drawBehind {
            with(shadow) { draw(elevation, shape, filterColor = color) }
        }
    },
    inspectorInfo = debugInspectorInfo {
        name = "colorShadow"
        properties["elevation"] = elevation
        properties["shape"] = shape
        properties["color"] = color
    }
)