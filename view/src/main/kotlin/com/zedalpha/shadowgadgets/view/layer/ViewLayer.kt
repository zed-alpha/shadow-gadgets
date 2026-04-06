package com.zedalpha.shadowgadgets.view.layer

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.view.View
import android.view.View.LAYER_TYPE_HARDWARE
import android.view.View.LAYER_TYPE_NONE
import com.zedalpha.shadowgadgets.view.internal.BaseView
import com.zedalpha.shadowgadgets.view.internal.fastLayout
import com.zedalpha.shadowgadgets.view.internal.obtainViewPainter

internal class ViewLayer(link: View, content: (Canvas) -> Unit) :
    AbstractLayer(link, content) {

    private var layerView = LayerView(link, content)

    private val painter = link.obtainViewPainter()

    init {
        painter?.add(layerView)
    }

    override fun dispose() {
        painter?.remove(layerView)
    }

    override fun updateLayerBounds() =
        with(bounds) { layerView.fastLayout(left, top, right, bottom) }

    override fun updateLayerType() {
        with(layerView) {
            if (isOffscreen) {
                setLayerType(LAYER_TYPE_HARDWARE, paint)
                overlappingRendering = true
            } else {
                setLayerType(LAYER_TYPE_NONE, paint)
                overlappingRendering = false
            }
        }
    }

    override fun drawLayer(canvas: Canvas) {
        painter?.run {
            val view = layerView
            view.superInvalidate()
            drawView(canvas, view)
        }
    }

    override fun recreateLayer() {
        val newView = LayerView(link, content)
        painter?.run { remove(layerView); add(newView) }
        layerView = newView
    }
}

@SuppressLint("ViewConstructor")
private class LayerView(
    link: View,
    private val content: (Canvas) -> Unit
) : BaseView(link.context) {

    var overlappingRendering: Boolean = false

    override fun hasOverlappingRendering(): Boolean = overlappingRendering

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) = content(canvas)
}