package com.zedalpha.shadowgadgets.compose.internal

import android.os.Build
import androidx.annotation.CallSuper
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

    override fun reset() = androidPath.reset()

    override fun setRectangle(rect: Rect) {
        super.setRectangle(rect)
        androidPath.addRect(
            rect.left,
            rect.top,
            rect.right,
            rect.bottom,
            AndroidPath.Direction.CW
        )
    }

    override fun setRoundedSimple(roundRect: RoundRect) {
        super.setRoundedSimple(roundRect)
        val radius = roundRect.topLeftCornerRadius.x
        androidPath.addRoundRect(
            roundRect.left,
            roundRect.top,
            roundRect.right,
            roundRect.bottom,
            radius,
            radius,
            AndroidPath.Direction.CW
        )
    }

    override fun setGeneric(composePath: Path) {
        super.setGeneric(composePath)
        androidPath.set(composePath.asAndroidPath())
    }
}

internal open class ShadowOutline {

    val androidOutline = AndroidOutline().apply { alpha = 1.0F }

    protected open fun reset() {}

    fun setShape(
        shape: Shape,
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ) {
        reset()

        val composeOutline = shape.createOutline(size, layoutDirection, density)

        when (composeOutline) {
            is ComposeOutline.Rectangle -> setRectangle(composeOutline.rect)

            is ComposeOutline.Rounded -> if (composeOutline.roundRect.isSimple) {
                setRoundedSimple(composeOutline.roundRect)
            } else {
                setRoundedComplex(composeOutline.roundRect)
            }

            is ComposeOutline.Generic -> setGeneric(composeOutline.path)
        }
    }

    @CallSuper
    protected open fun setRectangle(rect: Rect) =
        androidOutline.setRect(
            rect.left.fastRoundToInt(),
            rect.top.fastRoundToInt(),
            rect.right.fastRoundToInt(),
            rect.bottom.fastRoundToInt()
        )

    @CallSuper
    protected open fun setRoundedSimple(roundRect: RoundRect) =
        androidOutline.setRoundRect(
            roundRect.left.fastRoundToInt(),
            roundRect.top.fastRoundToInt(),
            roundRect.right.fastRoundToInt(),
            roundRect.bottom.fastRoundToInt(),
            roundRect.topLeftCornerRadius.x
        )

    private fun setRoundedComplex(roundRect: RoundRect) {
        val tmp = tmpPath ?: Path().also { tmpPath = it }
        tmp.reset()
        tmp.addRoundRect(roundRect)
        setGeneric(tmp)
    }

    @CallSuper
    protected open fun setGeneric(composePath: ComposePath) {
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
    }

    private var tmpPath: Path? = null
}

@RequiresApi(30)
private object OutlinePathHelper {

    @DoNotInline
    fun setPath(outline: AndroidOutline, path: AndroidPath) =
        outline.setPath(path)
}