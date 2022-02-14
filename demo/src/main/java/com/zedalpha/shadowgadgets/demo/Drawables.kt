package com.zedalpha.shadowgadgets.demo

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt


// Q&D background drawables with extra magic.

sealed class PaintingDrawable(@ColorInt color: Int) : Drawable() {
    protected val paint =
        Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG).apply { this.color = color }

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}

class MainDrawable(private val numCol: Int = 23) : PaintingDrawable(0xff000000.toInt()) {
    override fun onBoundsChange(bounds: Rect) {
        paint.shader = LinearGradient(
            0F,
            0F,
            bounds.width().toFloat(),
            bounds.height().toFloat(),
            0x11446688,
            0x77886644,
            Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas) {
        // https://math.stackexchange.com/q/49020
        val width = bounds.width()
        val height = bounds.height()
        val fitWidth = width * 1.1F
        val fitHeight = height * 1.1F

        canvas.save()
        canvas.translate(-width * .05F, -height * .05F)
        canvas.rotate(-4F, fitWidth / 2, fitHeight / 2)
        val dim = fitWidth / numCol
        for (col in 0..numCol) {
            val x = (dim * col)
            canvas.drawLine(x, 0F, x, fitHeight, paint)
        }
        var y = 0F
        while (y < fitHeight) {
            y += dim
            canvas.drawLine(0F, y, fitWidth, y, paint)
        }
        canvas.restore()
    }
}

class PlatformDrawable : PaintingDrawable(0x44ffbbbb) {
    private val path = Path()

    override fun draw(canvas: Canvas) {
        val outBounds = Rect(bounds).apply { inset(-bounds.width() / 5, -bounds.height() / 5) }
        val leftRange = (outBounds.left..outBounds.centerX())
        val rightRange = (outBounds.centerX()..outBounds.right)
        val topRange = (outBounds.top..outBounds.centerY())
        val bottomRange = (outBounds.centerY()..outBounds.bottom)

        path.apply {
            reset()
            moveTo(leftRange.random().toFloat(), outBounds.top.toFloat())
            lineTo(outBounds.right.toFloat(), bottomRange.random().toFloat())
            lineTo(outBounds.left.toFloat(), bottomRange.random().toFloat())
            close()
        }
        canvas.drawPath(path, paint)

        path.apply {
            reset()
            moveTo(outBounds.right.toFloat(), topRange.random().toFloat())
            lineTo(rightRange.random().toFloat(), outBounds.bottom.toFloat())
            lineTo(outBounds.left.toFloat(), outBounds.bottom.toFloat())
            close()
        }
        canvas.drawPath(path, paint)
    }
}

class CompatDrawable : PaintingDrawable(0x66bbffbb) {
    private class Circle(val x: Float, val y: Float, val radius: Float)

    private val circles = mutableListOf<Circle>()

    override fun onBoundsChange(bounds: Rect) {
        val width = (0..bounds.width())
        val height = (0..bounds.height())
        val halfShort = (0..minOf(bounds.width(), bounds.height()) / 2)

        repeat(7) {
            circles += Circle(
                width.random().toFloat(),
                height.random().toFloat(),
                halfShort.random().toFloat()
            )
        }
    }

    override fun draw(canvas: Canvas) {
        circles.forEach { canvas.drawCircle(it.x, it.y, it.radius, paint) }
    }
}

class MaterialDrawable : PaintingDrawable(0x44bbbbff) {
    private val rectangles = mutableListOf<Rect>()

    override fun onBoundsChange(bounds: Rect) {
        val width = (0..bounds.width())
        val height = (0..bounds.height())
        val halfWidth = (bounds.width() / 2..bounds.width())
        val halfHeight = (bounds.height() / 2..bounds.height())

        repeat(4) {
            val left = halfWidth.first - width.random()
            val top = halfHeight.first - height.random()
            rectangles += Rect(
                left,
                top,
                left + halfWidth.random(),
                top + halfHeight.random()
            )
        }
    }

    override fun draw(canvas: Canvas) {
        rectangles.forEach { canvas.drawRect(it, paint) }
    }
}