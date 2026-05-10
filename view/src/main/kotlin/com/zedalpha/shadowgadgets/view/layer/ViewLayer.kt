package com.zedalpha.shadowgadgets.view.layer

import android.annotation.SuppressLint
import android.content.Context
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

    private var view = createView()

    private val painter = link.obtainViewPainter()

    init {
        updatePaint()
        painter?.add(view)
    }

    override fun dispose() {
        painter?.remove(view)
    }

    override fun updateBounds() =
        with(bounds) { view.fastLayout(left, top, right, bottom) }

    override fun updateLayer(offscreen: Boolean, paint: Paint?) {
        val layerType = if (offscreen) LAYER_TYPE_HARDWARE else LAYER_TYPE_NONE
        view.setLayerType(layerType, paint)
    }

    override fun drawLayer(canvas: Canvas) {
        val painter = this.painter ?: return

        val view = this.view
        view.superInvalidate()
        painter.drawView(canvas, view)
    }

    override fun recreateLayer() {
        val painter = this.painter ?: return

        painter.remove(view)
        val next = createView()
        painter.add(next)
        view = next
    }

    private fun createView() = LayerView(link.context, content)
}

@SuppressLint("ViewConstructor")
private class LayerView(
    context: Context,
    private val content: (Canvas) -> Unit
) : BaseView(context) {

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) = content(canvas)
}