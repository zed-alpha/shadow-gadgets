package com.zedalpha.shadowgadgets.demo.internal

import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable

internal abstract class DemoShadowDrawable(view: View, clipped: Boolean) :
    ShadowDrawable(view, clipped) {

    private val path = Path()

    init {
        elevation = 40F
        setClipPathProvider { it.set(path) }
    }

    final override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)

        val sideLength = 0.5F * minOf(bounds.width(), bounds.height())
        pivotX = sideLength / 2F
        pivotY = sideLength / 2F

        centerShadow(bounds, sideLength)

        val outline = Outline()
        if (Build.VERSION.SDK_INT >= 30) {
            path.setToPuzzlePiece(sideLength)
            outline.setPath(path)
        } else {
            path.setToCompassPointer(sideLength)
            @Suppress("DEPRECATION")
            outline.setConvexPath(path)
        }
        outline.alpha = 1.0F
        setOutline(outline)
    }

    protected abstract fun centerShadow(bounds: Rect, sideLength: Float)
}

@RequiresApi(29)
private fun Path.setToPuzzlePiece(sideLength: Float) {
    val q = sideLength / 4

    reset()

    // top
    moveTo(0F, q)
    lineTo(q, q)
    arcTo(q, 0F, 2 * q, q, 100F, 340F, false)
    lineTo(2 * q, q)
    lineTo(3 * q, q)

    // right
    lineTo(3 * q, 2 * q)
    arcTo(3 * q, 2 * q, 4 * q, 3 * q, 190F, 340F, false)
    lineTo(3 * q, 3 * q)
    lineTo(3 * q, 4 * q)

    // bottom
    lineTo(2 * q, 4 * q)
    arcTo(q, 3 * q, 2 * q, 4 * q, 80F, -340F, false)
    lineTo(q, 4 * q)
    lineTo(0F, 4 * q)

    // left
    lineTo(0F, 3 * q)
    arcTo(0F, 2 * q, q, 3 * q, 170F, -340F, false)
    lineTo(0F, 2 * q)
    lineTo(0F, q)

    close()
}

private fun Path.setToCompassPointer(sideLength: Float) {
    val h = sideLength / 2

    reset()
    addRoundRect(
        0F, 0F,
        sideLength, sideLength,
        floatArrayOf(0F, 0F, h, h, 0F, 0F, h, h),
        Path.Direction.CW
    )
}