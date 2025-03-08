package com.zedalpha.shadowgadgets.core.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.View.LAYER_TYPE_HARDWARE
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.core.shadow.BaseView
import com.zedalpha.shadowgadgets.core.shadow.ViewPainterProxy

internal class ViewLayer(
    private val ownerView: View,
    override val layerDraw: LayerDraw,
) : ManagedLayer {

    private fun createLayerView() = object : BaseView(ownerView.context) {

        init {
            isVisible = false
            layout(0, 0, ownerView.width, ownerView.height)
        }

        override fun onDraw(canvas: Canvas) = layerDraw.draw(canvas)
    }

    private var layerView = createLayerView()

    private val painter = ViewPainterProxy(ownerView, layerView)

    override fun recreate() {
        val newLayerView = createLayerView()
        painter.replaceLayerView(layerView, newLayerView)
        layerView = newLayerView
    }

    override fun dispose() = painter.dispose()

    override fun setSize(width: Int, height: Int) =
        layerView.layout(0, 0, width, height)

    override fun setLayerPaint(paint: Paint) =
        layerView.setLayerType(LAYER_TYPE_HARDWARE, paint)

    override fun draw(canvas: Canvas) =
        painter.drawLayerView(canvas, layerView)

    override fun invalidate() = painter.invalidateLayerView(layerView)

    override fun refresh() = painter.refreshLayerView(layerView)
}