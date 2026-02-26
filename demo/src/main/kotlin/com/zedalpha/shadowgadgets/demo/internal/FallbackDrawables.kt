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

internal fun FallbackDrawable(context: Context) =
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
internal class ShadowDrawable(density: Float) : FallbackDrawable(density) {

    init {
        paint.apply {
            // radius isn't the same thing here, but the value works.
            setShadowLayer(radius, 0F, 0F, FallbackIndicatorColor)
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

internal class OutlineDrawable(density: Float) : FallbackDrawable(density) {

    init {
        paint.apply {
            color = FallbackIndicatorColor
            style = Paint.Style.STROKE
            strokeWidth = 5 * density
        }
    }
}

private const val FallbackIndicatorColor = 0xaaff00ff.toInt()