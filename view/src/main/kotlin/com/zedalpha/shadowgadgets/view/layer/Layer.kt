package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.view.View
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.isTint
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.tintOutlineShadow

internal interface Layer {
    var color: Int
    var bounds: Rect
    fun draw(canvas: Canvas)
    fun recreate()
    fun dispose()
}

internal fun Layer(
    link: View,
    content: (Canvas) -> Unit
): Layer =
    if (RenderNodeFactory.isOpen) {
        RenderNodeLayer(link, content)
    } else {
        ViewLayer(link, content)
    }

internal abstract class AbstractLayer(
    protected val link: View,
    protected val content: (Canvas) -> Unit
) : Layer {

    protected val paint = Paint()

    final override var color: Int = DefaultShadowColor
        set(color) {
            if (field == color) return
            field = color
            paint.setTint(color)
            updateLayerPaint()
        }

    protected abstract fun updateLayerPaint()

    final override var bounds = Rect()
        set(value) {
            if (field == value) return
            field.set(value)
            updateLayerBounds()
        }

    protected abstract fun updateLayerBounds()

    final override fun draw(canvas: Canvas) {
        if (canvas.isHardwareAccelerated) drawLayer(canvas)
    }

    protected abstract fun drawLayer(canvas: Canvas)

    final override fun recreate() {
        if (paint.colorFilter == null) return

        recreateLayer()
        updateLayerPaint()
        updateLayerBounds()
    }

    protected abstract fun recreateLayer()
}

private fun Paint.setTint(color: Int) =
    if (color.isTint) {
        this.alpha = 255
        this.colorFilter =
            ColorMatrixColorFilter(
                floatArrayOf(
                    0F, 0F, 0F, 0F, Color.red(color).toFloat(),
                    0F, 0F, 0F, 0F, Color.green(color).toFloat(),
                    0F, 0F, 0F, 0F, Color.blue(color).toFloat(),
                    0F, 0F, 0F, Color.alpha(color) / 255F, 0F
                )
            )
    } else {
        this.alpha = Color.alpha(color)
        this.colorFilter = null
    }

internal val ClipRequiresLayer = Build.VERSION.SDK_INT in 24..28

internal fun View.desiredLayerColor(): Int? =
    when {
        this.tintOutlineShadow -> this.outlineShadowColorCompat
        this.clipOutlineShadow && ClipRequiresLayer -> DefaultShadowColor
        else -> null
    }