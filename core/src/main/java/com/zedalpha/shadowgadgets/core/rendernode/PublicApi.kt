package com.zedalpha.shadowgadgets.core.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.RenderNode
import androidx.annotation.RequiresApi

@RequiresApi(29)
internal class RenderNodeApi29 : RenderNodeWrapper {

    private val renderNode = RenderNode(RenderNodeName)

    override var alpha: Float by renderNode::alpha

    override var cameraDistance: Float by renderNode::cameraDistance

    override var elevation: Float by renderNode::elevation

    override var pivotX: Float by renderNode::pivotX

    override var pivotY: Float by renderNode::pivotY

    override var rotationX: Float by renderNode::rotationX

    override var rotationY: Float by renderNode::rotationY

    override var rotationZ: Float by renderNode::rotationZ

    override var scaleX: Float by renderNode::scaleX

    override var scaleY: Float by renderNode::scaleY

    override var translationX: Float by renderNode::translationX

    override var translationY: Float by renderNode::translationY

    override var translationZ: Float by renderNode::translationZ

    override var ambientColor: Int by renderNode::ambientShadowColor

    override var spotColor: Int by renderNode::spotShadowColor

    override fun hasIdentityMatrix(): Boolean =
        renderNode.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) {
        renderNode.getMatrix(outMatrix)
    }

    override fun setOutline(outline: Outline?) {
        renderNode.setOutline(outline)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRenderNode(renderNode)
    }

    override fun setClipToBounds(clipToBounds: Boolean): Boolean {
        return renderNode.setClipToBounds(clipToBounds)
    }

    override fun setProjectBackwards(shouldProject: Boolean): Boolean {
        return renderNode.setProjectBackwards(shouldProject)
    }

    override fun setProjectionReceiver(shouldReceive: Boolean): Boolean {
        return renderNode.setProjectionReceiver(shouldReceive)
    }

    override fun setPosition(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Boolean {
        return renderNode.setPosition(left, top, right, bottom)
    }

    override fun beginRecording(width: Int, height: Int): Canvas {
        return renderNode.beginRecording(width, height)
    }

    override fun endRecording(canvas: Canvas) {
        renderNode.endRecording()
    }

    override fun hasDisplayList(): Boolean {
        return renderNode.hasDisplayList()
    }
}