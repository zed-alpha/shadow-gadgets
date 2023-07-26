package com.zedalpha.shadowgadgets.demo.view

import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.annotation.ColorInt


internal class SlantGridDrawable(
    @ColorInt private val startColor: Int = 0xFFCCCCCC.toInt(),
    @ColorInt private val endColor: Int = 0xFFCECECE.toInt(),
    private val columns: Int = 19
) : Drawable() {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

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
        canvas.drawColor(Color.WHITE)
        val bounds = bounds
        val paint = paint
        val columns = columns

        val width = bounds.width()
        val height = bounds.height()
        // https://math.stackexchange.com/q/49020
        val fitWidth = width * 1.1F
        val fitHeight = height * 1.1F

        canvas.save()
        canvas.clipRect(bounds)
        canvas.translate(-width * .05F, -height * .05F)
        canvas.rotate(-4F, fitWidth / 2, fitHeight / 2)
        val dim = fitWidth / columns
        for (col in 0..columns) {
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

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter?) {}
}