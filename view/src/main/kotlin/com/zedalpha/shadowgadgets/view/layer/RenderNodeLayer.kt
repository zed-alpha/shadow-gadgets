package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeWrapper
import com.zedalpha.shadowgadgets.view.rendernode.record

internal class RenderNodeLayer(link: View, content: (Canvas) -> Unit) :
    AbstractLayer(link, content) {

    private var layerNode = createLayerNode()

    init {
        updatePaint()
    }

    override fun dispose() = layerNode.discardDisplayList()

    override fun updateBounds() {
        with(bounds) { layerNode.setPosition(left, top, right, bottom) }
    }

    override fun updateLayer(offscreen: Boolean, paint: Paint?) {
        layerNode.setUseCompositingLayer(offscreen, paint)
    }

    override fun drawLayer(canvas: Canvas) {
        val node = layerNode
        node.record { content(it) }
        node.drawRenderNode(canvas)
    }

    override fun recreateLayer() {
        layerNode.discardDisplayList()
        layerNode = createLayerNode()
    }

    private fun createLayerNode(): RenderNodeWrapper =
        RenderNodeFactory.create("Layer")
}