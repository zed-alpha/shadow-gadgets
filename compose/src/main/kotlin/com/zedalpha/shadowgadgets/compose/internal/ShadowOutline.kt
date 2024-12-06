package com.zedalpha.shadowgadgets.compose.internal

import android.os.Build
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSimple
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.fastRoundToInt
import android.graphics.Outline as AndroidOutline
import android.graphics.Path as AndroidPath
import androidx.compose.ui.graphics.Outline as ComposeOutline
import androidx.compose.ui.graphics.Path as ComposePath

internal class ClippedOutline : ShadowOutline() {

    val androidPath = AndroidPath()

    override fun apply(composeOutline: ComposeOutline) {
        applyOutline(composeOutline, androidOutline, androidPath)
    }
}

internal class CompatOutline : ShadowOutline()

internal abstract class ShadowOutline {

    val androidOutline = AndroidOutline().apply { alpha = 1.0F }

    fun setShape(
        shape: Shape,
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) {
        apply(shape.createOutline(size, layoutDirection, density))
    }

    protected open fun apply(composeOutline: ComposeOutline) {
        applyOutline(composeOutline, androidOutline, null)
    }

    protected fun applyOutline(
        composeOutline: ComposeOutline,
        androidOutline: AndroidOutline,
        androidPath: AndroidPath?
    ) {
        androidPath?.reset()
        when (composeOutline) {
            is ComposeOutline.Rectangle -> {
                setRectangle(composeOutline.rect, androidOutline, androidPath)
            }
            is ComposeOutline.Rounded -> if (composeOutline.roundRect.isSimple) {
                setRoundedSimple(
                    composeOutline.roundRect,
                    androidOutline,
                    androidPath
                )
            } else {
                setRoundedComplex(
                    composeOutline.roundRect,
                    androidOutline,
                    androidPath
                )
            }
            is ComposeOutline.Generic -> {
                setGeneric(composeOutline.path, androidOutline, androidPath)
            }
        }
    }

    private fun setRectangle(
        rect: Rect,
        androidOutline: AndroidOutline,
        androidPath: AndroidPath?
    ) {
        androidOutline.setRect(
            rect.left.fastRoundToInt(),
            rect.top.fastRoundToInt(),
            rect.right.fastRoundToInt(),
            rect.bottom.fastRoundToInt()
        )
        androidPath?.addRect(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            AndroidPath.Direction.CW
        )
    }

    private fun setRoundedSimple(
        roundRect: RoundRect,
        androidOutline: AndroidOutline,
        androidPath: AndroidPath?
    ) {
        val radius = roundRect.topLeftCornerRadius.x
        androidOutline.setRoundRect(
            roundRect.left.fastRoundToInt(),
            roundRect.top.fastRoundToInt(),
            roundRect.right.fastRoundToInt(),
            roundRect.bottom.fastRoundToInt(),
            radius
        )
        androidPath?.addRoundRect(
            roundRect.left,
            roundRect.top,
            roundRect.right,
            roundRect.bottom,
            radius,
            radius,
            AndroidPath.Direction.CW
        )
    }

    private fun setRoundedComplex(
        roundRect: RoundRect,
        androidOutline: AndroidOutline,
        androidPath: AndroidPath?
    ) {
        val tmp = tmpPath ?: Path().also { tmpPath = it }
        tmp.reset()
        tmp.addRoundRect(roundRect)
        setGeneric(tmp, androidOutline, androidPath)
    }

    private fun setGeneric(
        composePath: ComposePath,
        androidOutline: AndroidOutline,
        androidPath: AndroidPath?
    ) {
        if (composePath.isEmpty) return

        val path = composePath.asAndroidPath()
        when {
            Build.VERSION.SDK_INT >= 30 -> {
                OutlinePathHelper.setPath(androidOutline, path)
            }
            Build.VERSION.SDK_INT >= 29 -> {
                try {
                    @Suppress("DEPRECATION")
                    androidOutline.setConvexPath(path)
                } catch (e: IllegalArgumentException) {
                    return  // Leave androidPath empty
                }
            }
            else -> {
                @Suppress("DEPRECATION")
                if (path.isConvex) androidOutline.setConvexPath(path)
            }
        }
        androidPath?.set(path)
    }

    private var tmpPath: Path? = null
}

@RequiresApi(30)
private object OutlinePathHelper {

    @DoNotInline
    fun setPath(outline: AndroidOutline, path: AndroidPath) =
        outline.setPath(path)
}