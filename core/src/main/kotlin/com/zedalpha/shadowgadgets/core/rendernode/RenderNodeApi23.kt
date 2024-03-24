package com.zedalpha.shadowgadgets.core.rendernode

import android.graphics.Canvas
import android.os.Build
import android.view.DisplayListCanvas
import android.view.RenderNode
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi

@RequiresApi(23)
internal class RenderNodeApi23(name: String?) : RenderNodeApi21(name) {

    override fun draw(canvas: Canvas) {
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as DisplayListCanvas).drawRenderNode(renderNode)
    }

    private fun recordEmptyDisplayList() {
        val canvas = renderNode.start(0, 0)
        renderNode.end(canvas)
    }

    override fun beginRecording(width: Int, height: Int): Canvas =
        renderNode.start(width, height)

    override fun endRecording(canvas: Canvas) {
        renderNode.end(canvas as DisplayListCanvas)
    }

    override fun discardDisplayList() {
        if (Build.VERSION.SDK_INT >= 24) {
            RenderNodeDiscardHelper24.discardDisplayList(renderNode)
        } else {
            RenderNodeDiscardHelper21.destroyDisplayListData(renderNode)
        }
    }
}

private object RenderNodeDiscardHelper21 {

    @DoNotInline
    fun destroyDisplayListData(renderNode: RenderNode) {
        renderNode.destroyDisplayListData()
    }
}

@RequiresApi(24)
private object RenderNodeDiscardHelper24 {

    @DoNotInline
    fun discardDisplayList(renderNode: RenderNode) {
        renderNode.discardDisplayList()
    }
}