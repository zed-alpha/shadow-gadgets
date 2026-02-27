package com.zedalpha.shadowgadgets.demo.internal

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.drawable.PaintDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.graphics.toRectF
import androidx.core.graphics.withSave

internal fun FallbackDrawable(context: Context): FallbackDrawable =
    if (Build.VERSION.SDK_INT > 28) {
        ShadowDrawable(context.resources.displayMetrics.density)
    } else {
        OutlineDrawable(context.resources.displayMetrics.density)
    }

internal abstract class FallbackDrawable(density: Float) : PaintDrawable() {

    protected val radius = 20 * density

    init {
        setCornerRadius(radius)
    }
}

@RequiresApi(28)
private class ShadowDrawable(density: Float) : FallbackDrawable(density) {

    init {
        paint.apply {
            // radius isn't the same thing here, but the value works.
            setShadowLayer(radius, 0F, 0F, Color.MAGENTA)
            this.color = Color.TRANSPARENT
        }
    }

    private val clip = Path()

    override fun onBoundsChange(bounds: Rect) {
        super.onBoundsChange(bounds)
        clip.rewind()
        clip.addRoundRect(bounds.toRectF(), radius, radius, Path.Direction.CW)
    }

    override fun draw(canvas: Canvas) =
        canvas.withSave {
            canvas.clipOutPath(clip)
            super.draw(canvas)
        }
}

private class OutlineDrawable(density: Float) : FallbackDrawable(density) {

    init {
        paint.apply {
            color = Color.MAGENTA
            style = Paint.Style.STROKE
            strokeWidth = 5 * density
        }
    }
}