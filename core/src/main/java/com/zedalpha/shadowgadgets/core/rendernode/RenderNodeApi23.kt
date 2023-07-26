package com.zedalpha.shadowgadgets.core.rendernode

import android.graphics.Canvas
import android.view.DisplayListCanvas
import androidx.annotation.RequiresApi

@RequiresApi(23)
internal class RenderNodeApi23(name: String?) : RenderNodeApi21(name) {

    override fun draw(canvas: Canvas) {
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as DisplayListCanvas).drawRenderNode(renderNode)
    }

    override fun beginRecording(width: Int, height: Int): Canvas =
        renderNode.start(width, height)

    override fun endRecording(canvas: Canvas) {
        renderNode.end(canvas as DisplayListCanvas)
    }

    private fun recordEmptyDisplayList() {
        val canvas = renderNode.start(0, 0)
        renderNode.end(canvas)
    }
}