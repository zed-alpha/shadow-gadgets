package com.zedalpha.shadowgadgets.demo.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Path
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import androidx.annotation.ColorInt
import androidx.core.graphics.withClip

internal class SlantGridDrawable(
    @ColorInt private val startColor: Int = 0xFFC0C0C0.toInt(),
    @ColorInt private val endColor: Int = 0xFFCECECE.toInt(),
    private val columns: Int = 19
) : PaintDrawable() {

    override fun onBoundsChange(bounds: Rect) {
        paint.shader = LinearGradient(
            0F,
            0F,
            bounds.width().toFloat(),
            bounds.height().toFloat(),
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
    }

    override fun draw(canvas: Canvas) {
        val bounds = bounds
        val paint = paint
        val columns = columns

        val width = bounds.width()
        val height = bounds.height()
        // https://math.stackexchange.com/q/49020
        val fitWidth = width * 1.1F
        val fitHeight = height * 1.1F

        canvas.withClip(bounds) {
            drawColor(Color.WHITE)
            translate(-width * .055F, -height * .055F)
            rotate(-4F, fitWidth / 2, fitHeight / 2)
            val dim = fitWidth / columns
            for (col in 0..columns) {
                val x = (dim * col)
                drawLine(x, 0F, x, fitHeight, paint)
            }
            var y = 0F
            while (y < fitHeight) {
                y += dim
                drawLine(0F, y, fitWidth, y, paint)
            }
        }
    }
}

internal class PlatformDrawable : PaintDrawable(0x44FFBBBB) {

    private class Triangle(val p0: PointF, val p1: PointF, val p2: PointF) {
        constructor(
            p0x: Float, p0y: Float,
            p1x: Float, p1y: Float,
            p2x: Float, p2y: Float
        ) : this(PointF(p0x, p0y), PointF(p1x, p1y), PointF(p2x, p2y))
    }

    private lateinit var triangles: Array<Triangle>

    private val path = Path()

    override fun onBoundsChange(bounds: Rect) {
        val outBounds = Rect(bounds)
        outBounds.inset(-bounds.width() / 5, -bounds.height() / 5)

        val leftRange = outBounds.left..outBounds.centerX()
        val rightRange = outBounds.centerX()..outBounds.right
        val topRange = outBounds.top..outBounds.centerY()
        val bottomRange = outBounds.centerY()..outBounds.bottom

        triangles = Array(2) { index ->
            when (index) {
                1 -> Triangle(
                    leftRange.random().toFloat(), outBounds.top.toFloat(),
                    outBounds.right.toFloat(), bottomRange.random().toFloat(),
                    outBounds.left.toFloat(), bottomRange.random().toFloat()
                )
                else -> Triangle(
                    outBounds.right.toFloat(), topRange.random().toFloat(),
                    rightRange.random().toFloat(), outBounds.bottom.toFloat(),
                    outBounds.left.toFloat(), outBounds.bottom.toFloat()
                )
            }
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.withClip(bounds) {
            drawColor(Color.WHITE)
            triangles.forEach { t ->
                path.apply {
                    reset()
                    moveTo(t.p0.x, t.p0.y)
                    lineTo(t.p1.x, t.p1.y)
                    lineTo(t.p2.x, t.p2.y)
                    close()
                    drawPath(this, paint)
                }
            }
        }
    }
}

internal class CompatDrawable : PaintDrawable(0x66BBFFBB) {

    private class Circle(val x: Float, val y: Float, val radius: Float)

    private lateinit var circles: Array<Circle>

    override fun onBoundsChange(bounds: Rect) {
        val width = 0..bounds.width()
        val height = 0..bounds.height()
        val halfShort = (0..minOf(bounds.width(), bounds.height()) / 2)

        circles = Array(7) {
            Circle(
                width.random().toFloat(),
                height.random().toFloat(),
                halfShort.random().toFloat()
            )
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.withClip(bounds) {
            drawColor(Color.WHITE)
            circles.forEach { drawCircle(it.x, it.y, it.radius, paint) }
        }
    }
}

internal class MaterialDrawable : PaintDrawable(0x44BBBBFF) {

    private lateinit var rectangles: Array<Rect>

    override fun onBoundsChange(bounds: Rect) {
        val width = 0..bounds.width()
        val height = 0..bounds.height()
        val halfWidth = (bounds.width() / 2..bounds.width())
        val halfHeight = (bounds.height() / 2..bounds.height())

        rectangles = Array(4) {
            val left = halfWidth.first - width.random()
            val top = halfHeight.first - height.random()
            Rect(
                left,
                top,
                left + halfWidth.random(),
                top + halfHeight.random()
            )
        }
    }

    override fun draw(canvas: Canvas) {
        canvas.withClip(bounds) {
            drawColor(Color.WHITE)
            rectangles.forEach { drawRect(it, paint) }
        }
    }
}