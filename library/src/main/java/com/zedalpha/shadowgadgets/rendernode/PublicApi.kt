package com.zedalpha.shadowgadgets.rendernode

import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.RenderNode
import android.os.Build
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.Q)
internal class RenderNodeApi29 : RenderNodeWrapper {
    private val renderNode = RenderNode("OverlayShadow")

    override fun setElevation(elevation: Float) {
        renderNode.elevation = elevation
    }

    override fun setTranslationZ(translationZ: Float) {
        renderNode.translationZ = translationZ
    }

    override fun setOutline(outline: Outline?) {
        renderNode.setOutline(outline)
    }

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        renderNode.setPosition(left, top, right, bottom)

    override fun draw(canvas: Canvas) {
        canvas.drawRenderNode(renderNode)
    }
}