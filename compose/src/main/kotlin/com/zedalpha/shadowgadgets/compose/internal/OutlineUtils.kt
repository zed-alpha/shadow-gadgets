package com.zedalpha.shadowgadgets.compose.internal

import android.graphics.Outline
import android.os.Build
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.util.fastRoundToInt
import android.graphics.Outline as AndroidOutline
import android.graphics.Path as AndroidPath

internal fun setRectangle(
    rect: Rect,
    androidOutline: AndroidOutline,
    androidPath: AndroidPath
) {
    androidOutline.setRect(
        rect.left.fastRoundToInt(),
        rect.top.fastRoundToInt(),
        rect.right.fastRoundToInt(),
        rect.bottom.fastRoundToInt()
    )
    androidPath.addRect(
        rect.left,
        rect.top,
        rect.right,
        rect.bottom,
        AndroidPath.Direction.CW
    )
}

internal fun setRoundedSimple(
    roundRect: RoundRect,
    androidOutline: Outline,
    androidPath: android.graphics.Path
) {
    val radius = roundRect.topLeftCornerRadius.x
    androidOutline.setRoundRect(
        roundRect.left.fastRoundToInt(),
        roundRect.top.fastRoundToInt(),
        roundRect.right.fastRoundToInt(),
        roundRect.bottom.fastRoundToInt(),
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
}

internal fun setRoundedComplex(
    roundRect: RoundRect,
    androidOutline: Outline,
    androidPath: android.graphics.Path,
    tmpPath: Path
) {
    tmpPath.reset()
    tmpPath.addRoundRect(roundRect)
    setGeneric(tmpPath, androidOutline, androidPath)
}

internal fun setGeneric(
    composePath: Path,
    androidOutline: AndroidOutline,
    androidPath: AndroidPath
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
    androidPath.set(path)
}

@RequiresApi(30)
private object OutlinePathHelper {

    @DoNotInline
    fun setPath(outline: AndroidOutline, path: AndroidPath) =
        outline.setPath(path)
}