package com.zedalpha.shadowgadgets.demo.internal

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.drawable.PaintDrawable
import androidx.annotation.ColorInt
import androidx.core.graphics.withClip

internal class SlantGridDrawable(
    @ColorInt private val startColor: Int = 0xffc0c0c0.toInt(),
    @ColorInt private val endColor: Int = 0xffcecece.toInt(),
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