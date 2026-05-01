package com.zedalpha.shadowgadgets.view.layer

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.View.LAYER_TYPE_HARDWARE
import android.view.View.LAYER_TYPE_NONE
import com.zedalpha.shadowgadgets.view.internal.BaseView
import com.zedalpha.shadowgadgets.view.internal.fastLayout
import com.zedalpha.shadowgadgets.view.internal.obtainViewPainter

internal class ViewLayer(link: View, content: (Canvas) -> Unit) :
    AbstractLayer(link, content) {

    private var layerView = createLayerView()

    private val painter = link.obtainViewPainter()

    init {
        updatePaint()
        painter?.add(layerView)
    }

    override fun dispose() {
        painter?.remove(layerView)
    }

    override fun updateBounds() =
        with(bounds) { layerView.fastLayout(left, top, right, bottom) }

    override fun updateLayer(offscreen: Boolean, paint: Paint?) {
        val layerType = if (offscreen) LAYER_TYPE_HARDWARE else LAYER_TYPE_NONE
        layerView.setLayerType(layerType, paint)
    }

    override fun drawLayer(canvas: Canvas) {
        painter?.run {
            val layer = layerView
            layer.superInvalidate()
            drawView(canvas, layer)
        }
    }

    override fun recreateLayer() {
        val nextView = createLayerView()
        painter?.run {
            remove(layerView)
            add(nextView)
        }
        layerView = nextView
    }

    private fun createLayerView(): LayerView = LayerView(link, content)
}

@SuppressLint("ViewConstructor")
private class LayerView(
    link: View,
    private val content: (Canvas) -> Unit
) : BaseView(link.context) {

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) = content(canvas)
}