package com.zedalpha.shadowgadgets.view.rendernode

import android.graphics.Canvas
import android.os.Build
import android.view.DisplayListCanvas
import android.view.RenderNode
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi

@RequiresApi(23)
internal class RenderNodeApi23(name: String?) : RenderNodeApi21(name) {

    override fun drawRenderNode(canvas: Canvas) {
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as DisplayListCanvas).drawRenderNode(renderNode)
    }

    private fun recordEmptyDisplayList() {
        val canvas = renderNode.start(0, 0)
        renderNode.end(canvas)
    }

    private var canvas: Canvas? = null

    override fun beginRecording(): Canvas =
        renderNode.start(width, height).also { canvas = it }

    override fun endRecording() {
        check(canvas != null) { "No recording in progress" }
        renderNode.end(canvas as DisplayListCanvas).also { canvas = null }
    }

    override fun discardDisplayList() =
        if (Build.VERSION.SDK_INT >= 24) {
            RenderNodeDiscardHelper24.discardDisplayList(renderNode)
        } else {
            super.discardDisplayList()
        }
}

@RequiresApi(24)
private object RenderNodeDiscardHelper24 {

    @DoNotInline
    fun discardDisplayList(renderNode: RenderNode) =
        renderNode.discardDisplayList()
}