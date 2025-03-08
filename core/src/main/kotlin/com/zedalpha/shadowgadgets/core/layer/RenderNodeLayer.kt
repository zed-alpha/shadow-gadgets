package com.zedalpha.shadowgadgets.core.layer

import android.graphics.Canvas
import android.graphics.Paint
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

internal class RenderNodeLayer(override val layerDraw: LayerDraw) :
    ManagedLayer {

    private var layerNode = RenderNodeFactory.newInstance("ManagedLayer")

    override fun recreate() {
        layerNode.discardDisplayList()
        layerNode = RenderNodeFactory.newInstance("ManagedLayer")
    }

    override fun dispose() = layerNode.discardDisplayList()

    private var width = 0

    private var height = 0

    override fun setSize(width: Int, height: Int) {
        this.width = width
        this.height = height
        layerNode.setPosition(0, 0, width, height)
    }

    override fun setLayerPaint(paint: Paint) {
        layerNode.setUseCompositingLayer(true, paint)
    }

    override fun draw(canvas: Canvas) {
        val node = layerNode
        val nodeCanvas = node.beginRecording(width, height)
        try {
            layerDraw.draw(nodeCanvas)
        } finally {
            node.endRecording(nodeCanvas)
        }
        node.draw(canvas)
    }
}