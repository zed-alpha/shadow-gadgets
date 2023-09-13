package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
internal fun Modifier.baseShadow(
    clipped: Boolean,
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor,
    colorCompat: Color = DefaultShadowColor
) = composed {
    val drawShadow = if (elevation > 0.dp) {
        val view = LocalView.current
        val shadow = remember(view, clipped) { ComposeShadow(view, clipped) }
        DisposableEffect(view, clipped) { onDispose { shadow.dispose() } }

        var offsetInRoot by remember { mutableStateOf(Offset.Zero) }
        val position = onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInRoot()
            shadow.prepare(bounds, ambientColor, spotColor, colorCompat)
            offsetInRoot = Offset(bounds.left, bounds.top)
        }

        var layerOffset by remember { mutableStateOf(InitialOffset) }
        if (requiresLayer(colorCompat)) LaunchedEffect(view) {
            view.screenLocation.collect { layerOffset = it }
        }
        val draw = drawBehind {
            shadow.draw(this, elevation, shape, offsetInRoot, layerOffset)
        }

        position then draw
    } else {
        this
    }
    if (clip) {
        drawShadow then clip(shape)
    } else {
        drawShadow
    }
}