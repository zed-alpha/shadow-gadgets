package com.zedalpha.shadowgadgets.demo

import android.graphics.*
import android.graphics.drawable.Drawable


// Q&D background drawables with extra magic.

sealed class PaintingDrawable(color: Int) : Drawable() {
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
        canvas.save()
        canvas.rotate(-4F, bounds.width() / 2F, bounds.height() / 2F)
        val dim = bounds.width() / numCol
        for (x in 0..numCol) {
            canvas.drawLine(
                (dim * x).toFloat(),
                0F,
                (dim * x).toFloat(),
                bounds.height().toFloat(),
                paint
            )
        }
        var y = 0F
        while (y < bounds.height()) {
            y += dim
            canvas.drawLine(0F, y, bounds.width().toFloat(), y, paint)
        }
        canvas.restore()
    }
}

class PlatformDrawable : PaintingDrawable(0x33ffbbbb) {
    private val path = Path()

    override fun draw(canvas: Canvas) {
        val width = bounds.width().toFloat()
        val height = bounds.height().toFloat()

        path.reset()
        path.moveTo(0F, height * 0.8F)
        path.lineTo(width, height * 0.1F)
        path.lineTo(width, height)
        path.lineTo(0F, height)
        path.close()
        canvas.drawPath(path, paint)

        path.reset()
        path.moveTo(0F, 0F)
        path.lineTo(width * 0.35F, 0F)
        path.lineTo(width, height * 0.7F)
        path.lineTo(width, height)
        path.lineTo(0F, height)
        path.close()
        canvas.drawPath(path, paint)
    }
}

class CompatDrawable : PaintingDrawable(0x77bbffbb) {
    private class Circle(val x: Float, val y: Float, val radius: Float)

    private val circles = mutableListOf<Circle>()

    override fun onBoundsChange(bounds: Rect) {
        val width = (0..bounds.width())
        val height = (0..bounds.height())
        val halfWidth = (0..bounds.width() / 2)

        repeat(7) {
            circles += Circle(
                width.random().toFloat(),
                height.random().toFloat(),
                halfWidth.random().toFloat()
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

        repeat(5) {
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