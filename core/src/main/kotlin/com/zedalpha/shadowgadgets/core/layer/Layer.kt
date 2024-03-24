package com.zedalpha.shadowgadgets.core.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.view.View
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

class Layer(
    ownerView: View,
    color: Int,
    private var width: Int,
    private var height: Int,
) {
    private val layer: ManagedLayer = when {
        RenderNodeFactory.isOpen -> RenderNodeLayer(::contentDraw)
        else -> ViewLayer(ownerView, ::contentDraw)
    }.apply { setSize(width, height) }

    private val paint = Paint()

    var color: Int = Color.TRANSPARENT
        set(value) {
            if (field == value) return
            field = value
            paint.setLayerFilter(color)
            layer.setLayerPaint(paint)
        }

    init {
        this.color = color
    }

    fun recreate() {
        layer.apply {
            recreate()
            setLayerPaint(paint)
            setSize(width, height)
        }
    }

    fun dispose() {
        layer.dispose()
    }

    fun setSize(width: Int, height: Int) {
        if (this.width == width && this.height == height) return
        this.width = width; this.height = height
        layer.setSize(width, height)
    }

    private val layerDraws = mutableListOf<LayerDraw>()

    private fun contentDraw(canvas: Canvas) {
        layerDraws.forEach { it.draw(canvas) }
    }

    fun addDraw(draw: LayerDraw) {
        layerDraws.add(draw)
    }

    fun removeDraw(draw: LayerDraw) {
        layerDraws.remove(draw)
    }

    fun isEmpty() = layerDraws.isEmpty()

    fun draw(canvas: Canvas) {
        layer.draw(canvas)
    }

    fun invalidate() {
        layer.invalidate()
    }

    fun refresh() {
        layer.refresh()
    }
}

private fun Paint.setLayerFilter(color: Int) {
    if (color == DefaultShadowColorInt) {
        alpha = 255
        colorFilter = null
    } else {
        alpha = Color.alpha(color)
        colorFilter = ColorMatrixColorFilter(
            floatArrayOf(
                0F, 0F, 0F, 0F, Color.red(color).toFloat(),
                0F, 0F, 0F, 0F, Color.green(color).toFloat(),
                0F, 0F, 0F, 0F, Color.blue(color).toFloat(),
                0F, 0F, 0F, 1F, 0F
            )
        )
    }
}