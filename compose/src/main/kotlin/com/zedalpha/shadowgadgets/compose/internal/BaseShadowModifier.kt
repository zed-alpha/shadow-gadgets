package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
internal fun Modifier.baseShadow(
    clipped: Boolean,
    elevation: Dp,
    shape: Shape,
    clip: Boolean,
    ambientColor: Color,
    spotColor: Color,
    colorCompat: Color
): Modifier = composed {
    val drawShadow = if (elevation > 0.dp) {
        val view = LocalView.current
        val shadow = remember(view, clipped) { ComposeShadow(view, clipped) }
        DisposableEffect(shadow) { onDispose { shadow.dispose() } }

        val useLayer = requiresLayer(colorCompat)
        val layer = if (useLayer) rememberComposeLayer(shadow) else null

        this
            .onGloballyPositioned { coordinates ->
                shadow.setPosition(coordinates, useLayer)
                layer?.setPosition(coordinates)
            }
            .drawWithCache {
                shadow.setShape(shape, size, layoutDirection, this)
                onDrawBehind {
                    shadow.prepareDraw(
                        elevation.toPx(),
                        ambientColor,
                        spotColor,
                        useLayer
                    )
                    if (layer != null) {
                        layer.refresh()
                        layer.color = colorCompat
                        layer.draw(drawContext.canvas.nativeCanvas)
                    } else {
                        shadow.draw(drawContext.canvas.nativeCanvas)
                    }
                }
            }
    } else {
        this
    }
    if (clip) drawShadow.clip(shape) else drawShadow
}