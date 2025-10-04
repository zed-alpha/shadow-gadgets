package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.view.View
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.rendernode.record

internal class RenderNodeLayer(link: View, content: (Canvas) -> Unit) :
    AbstractLayer(link, content) {

    private var layerNode = RenderNodeFactory.create("Layer")

    override fun dispose() {
        layerNode.discardDisplayList()
    }

    override fun updateLayerBounds() {
        with(bounds) { layerNode.setPosition(left, top, right, bottom) }
    }

    override fun updateLayerType() {
        with(layerNode) {
            if (isOffscreen) {
                setUseCompositingLayer(true, paint)
                setHasOverlappingRendering(true)
            } else {
                setUseCompositingLayer(false, paint)
                setHasOverlappingRendering(false)
            }
        }
    }

    override fun drawLayer(canvas: Canvas) {
        val node = layerNode
        node.record { content(it) }
        node.drawRenderNode(canvas)
    }

    override fun recreateLayer() {
        layerNode.discardDisplayList()
        layerNode = RenderNodeFactory.create("Layer")
    }
}