package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.RectF
import androidx.annotation.ColorInt


class ShadowColorFilter(private val layerBounds: RectF? = null) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    @ColorInt
    var color: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value
            paint.colorFilter = if (value != DefaultShadowColorInt) {
                ColorMatrixColorFilter(
                    floatArrayOf(
                        1F, 0F, 0F, 0F, Color.red(value).toFloat(),
                        0F, 1F, 0F, 0F, Color.green(value).toFloat(),
                        0F, 0F, 1F, 0F, Color.blue(value).toFloat(),
                        0F, 0F, 0F, 1F, 0F,
                    )
                )
            } else null
        }

    fun draw(canvas: Canvas, shadow: Shadow) {
        saveLayer(canvas)
        shadow.draw(canvas)
        restore(canvas)
    }

    fun saveLayer(canvas: Canvas) {
        canvas.saveLayer(layerBounds, paint)
    }

    fun restore(canvas: Canvas) {
        canvas.restore()
    }
}