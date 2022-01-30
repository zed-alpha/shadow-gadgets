package com.zedalpha.shadowgadgets.rendernode

import android.graphics.Canvas
import android.graphics.Outline
import android.view.DisplayListCanvas
import android.view.HardwareCanvas
import android.view.RenderNode
import androidx.annotation.RequiresApi


internal open class RenderNodeApi21 : RenderNodeWrapper {
    protected val renderNode: RenderNode = RenderNode.create("OverlayShadow", null)

    init {
        renderNode.end(renderNode.start(0, 0))
    }

    override fun setElevation(elevation: Float) {
        renderNode.setElevation(elevation)
    }

    override fun setTranslationZ(translationZ: Float) {
        renderNode.setTranslationZ(translationZ)
    }

    override fun setOutline(outline: Outline?) {
        renderNode.setOutline(outline)
    }

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        renderNode.setLeftTopRightBottom(left, top, right, bottom)

    override fun draw(canvas: Canvas) {
        (canvas as HardwareCanvas).drawRenderNode(renderNode)
    }
}

@RequiresApi(23)
internal class RenderNodeApi23 : RenderNodeApi21() {
    override fun draw(canvas: Canvas) {
        (canvas as DisplayListCanvas).drawRenderNode(renderNode)
    }
}