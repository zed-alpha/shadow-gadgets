package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.view.View
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeWrapper
import com.zedalpha.shadowgadgets.view.rendernode.record

internal class RenderNodeLayer(link: View, content: (Canvas) -> Unit) :
    AbstractLayer(link, content) {

    private var layerNode = createLayerNode()

    override fun dispose() = layerNode.discardDisplayList()

    override fun updateLayerBounds() {
        with(bounds) { layerNode.setPosition(left, top, right, bottom) }
    }

    override fun updateLayerPaint() {
        layerNode.setUseCompositingLayer(true, paint)
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