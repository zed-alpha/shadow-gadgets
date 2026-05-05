package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeWrapper
import com.zedalpha.shadowgadgets.view.rendernode.record

internal class RenderNodeLayer(link: View, content: (Canvas) -> Unit) :
    AbstractLayer(link, content) {

    private var renderNode = createRenderNode()

    init {
        updatePaint()
    }

    override fun dispose() = renderNode.discardDisplayList()

    override fun updateBounds() {
        with(bounds) { renderNode.setPosition(left, top, right, bottom) }
    }

    override fun updateLayer(offscreen: Boolean, paint: Paint?) {
        renderNode.setUseCompositingLayer(offscreen, paint)
    }

    override fun drawLayer(canvas: Canvas) {
        val node = renderNode
        node.record { content(it) }
        node.draw(canvas)
    }

    override fun recreateLayer() {
        renderNode.discardDisplayList()
        renderNode = createRenderNode()
    }

    private fun createRenderNode(): RenderNodeWrapper =
        RenderNodeFactory.create("Layer")
}