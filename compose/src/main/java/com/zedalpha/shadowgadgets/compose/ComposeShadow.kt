package com.zedalpha.shadowgadgets.compose

import android.os.Build
import android.view.View
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSimple
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.ShadowColorFilter
import kotlin.math.roundToInt
import android.graphics.Outline as AndroidOutline
import android.graphics.Path as AndroidPath


internal class ComposeShadow(
    localView: View,
    clipOutlineShadow: Boolean
) {
    private val tmpComposePath = Path()

    private val androidPath = AndroidPath()

    private val androidOutline = AndroidOutline().apply { alpha = 1.0F }

    private val shadow = if (clipOutlineShadow) {
        ClippedShadow(localView, { it.set(androidPath) })
    } else {
        Shadow(localView)
    }

    private val filter by lazy { ShadowColorFilter() }

    fun DrawScope.draw(
        elevation: Dp,
        shape: Shape,
        ambientColor: Color = DefaultShadowColor,
        spotColor: Color = DefaultShadowColor,
        filterColor: Color = DefaultShadowColor
    ) {
        val androidCanvas = drawContext.canvas.nativeCanvas
        if (!androidCanvas.isHardwareAccelerated) return

        setShape(shape, size, layoutDirection, this)
        with(shadow) {
            setOutline(androidOutline)
            this.elevation = elevation.toPx()
            this.ambientColor = ambientColor.toArgb()
            this.spotColor = spotColor.toArgb()
            if (filterColor == DefaultShadowColor) {
                draw(androidCanvas)
            } else {
                filter.color = filterColor.toArgb()
                filter.draw(androidCanvas, this)
            }
        }
    }

    fun dispose() {
        shadow.dispose()
    }

    private fun setShape(
        shape: Shape,
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) {
        androidPath.reset()
        when (val outline =
            shape.createOutline(size, layoutDirection, density)) {

            is Outline.Rectangle ->
                fromRect(outline.rect, androidOutline, androidPath)

            is Outline.Rounded ->
                fromRoundRect(outline.roundRect, androidOutline, androidPath)

            is Outline.Generic ->
                fromPath(outline.path, androidOutline, androidPath)
        }
    }

    private fun fromRect(
        rect: Rect,
        androidOutline: AndroidOutline,
        androidPath: AndroidPath
    ) {
        androidOutline.setRect(
            rect.left.roundToInt(),
            rect.top.roundToInt(),
            rect.right.roundToInt(),
            rect.bottom.roundToInt()
        )
        androidPath.addRect(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            AndroidPath.Direction.CW
        )
    }

    private fun fromRoundRect(
        roundRect: RoundRect,
        androidOutline: AndroidOutline,
        androidPath: AndroidPath
    ) {
        val radius = roundRect.topLeftCornerRadius.x
        if (roundRect.isSimple) {
            androidOutline.setRoundRect(
                roundRect.left.roundToInt(),
                roundRect.top.roundToInt(),
                roundRect.right.roundToInt(),
                roundRect.bottom.roundToInt(),
                radius
            )
            androidPath.addRoundRect(
                roundRect.left,
                roundRect.top,
                roundRect.right,
                roundRect.bottom,
                radius,
                radius,
                AndroidPath.Direction.CW
            )
        } else {
            fromPath(
                tmpComposePath.apply { reset(); addRoundRect(roundRect) },
                androidOutline,
                androidPath
            )
        }
    }

    private fun fromPath(
        composePath: Path,
        androidOutline: AndroidOutline,
        androidPath: AndroidPath
    ) {
        if (composePath.isEmpty) return

        val path = composePath.asAndroidPath()
        when {
            Build.VERSION.SDK_INT >= 30 -> {
                OutlinePath30.setPath(androidOutline, path)
            }

            Build.VERSION.SDK_INT >= 29 -> try {  // Early Q fumbled this
                @Suppress("DEPRECATION")
                androidOutline.setConvexPath(path)
            } catch (e: IllegalArgumentException) {
                return  // Leave androidPath empty
            }

            else -> {
                @Suppress("DEPRECATION")
                if (path.isConvex) androidOutline.setConvexPath(path)
            }
        }
        androidPath.set(path)
    }
}

@RequiresApi(30)
private object OutlinePath30 {
    @DoNotInline
    fun setPath(outline: AndroidOutline, path: AndroidPath) {
        outline.setPath(path)
    }
}