package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.core.layer.DefaultInlineLayerRequired

@Stable
internal fun Modifier.baseShadow(
    clipped: Boolean,
    elevation: Dp,
    shape: Shape,
    clip: Boolean,
    ambientColor: Color,
    spotColor: Color,
    colorCompat: Color
): Modifier =
    composed {
        val drawShadow = if (elevation > 0.dp) {

            val view = LocalView.current
            val useLayer = colorCompat != DefaultShadowColor ||
                    DefaultInlineLayerRequired
            val shadow = remember(view, clipped, useLayer) {
                ComposeShadow(view, clipped, useLayer)
            }
            DisposableEffect(shadow) { onDispose { shadow.dispose() } }

            this
                .onGloballyPositioned { shadow.setPosition(it) }
                .drawWithCache {
                    shadow.setShape(shape, size, layoutDirection, this)
                    onDrawBehind {
                        shadow.draw(
                            drawContext.canvas.nativeCanvas,
                            elevation.toPx(),
                            ambientColor,
                            spotColor,
                            colorCompat
                        )
                    }
                }
        } else {
            this
        }

        if (clip) drawShadow.clip(shape) else drawShadow
    }