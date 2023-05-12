package com.zedalpha.shadowgadgets.core.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.view.DisplayListCanvas
import android.view.HardwareCanvas
import android.view.RenderNode
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt


internal open class RenderNodeApi21 : RenderNodeWrapper {

    protected val renderNode = RenderNode.create(RenderNodeName, null)

    override var alpha: Float by renderNode::alpha

    override var cameraDistance: Float by renderNode::cameraDistance

    override var elevation: Float by renderNode::elevation

    override var pivotX: Float by renderNode::pivotX

    override var pivotY: Float by renderNode::pivotY

    override var rotationX: Float by renderNode::rotationX

    override var rotationY: Float by renderNode::rotationY

    override var rotationZ: Float by renderNode::rotation

    override var scaleX: Float by renderNode::scaleX

    override var scaleY: Float by renderNode::scaleY

    override var translationX: Float by renderNode::translationX

    override var translationY: Float by renderNode::translationY

    override var translationZ: Float by renderNode::translationZ

    override var ambientColor: Int
        get() = DefaultShadowColorInt
        set(@Suppress("UNUSED_PARAMETER") value) {}

    override var spotColor: Int
        get() = DefaultShadowColorInt
        set(@Suppress("UNUSED_PARAMETER") value) {}

    override fun hasIdentityMatrix() =
        renderNode.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) {
        renderNode.getMatrix(outMatrix)
    }

    override fun setOutline(outline: Outline?) {
        renderNode.setOutline(outline)
    }

    override fun draw(canvas: Canvas) {
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as HardwareCanvas).drawRenderNode(renderNode)
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
        return renderNode.setLeftTopRightBottom(left, top, right, bottom)
    }

    override fun beginRecording(width: Int, height: Int): Canvas =
        RenderNodeReflector.start(renderNode, width, height)

    override fun endRecording(canvas: Canvas) {
        RenderNodeReflector.end(renderNode, canvas as HardwareCanvas)
    }

    override fun hasDisplayList(): Boolean {
        return renderNode.isValid
    }

    private fun recordEmptyDisplayList() {
        val canvas = RenderNodeReflector.start(renderNode, 0, 0)
        RenderNodeReflector.end(renderNode, canvas)
    }
}

@RequiresApi(23)
internal class RenderNodeApi23 : RenderNodeApi21() {

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

private object RenderNodeReflector {

    private val startMethod = RenderNode::class.java.getDeclaredMethod(
        "start",
        Int::class.java,
        Int::class.java
    )

    private val endMethod = RenderNode::class.java.getDeclaredMethod(
        "end",
        HardwareCanvas::class.java
    )

    fun start(renderNode: RenderNode, width: Int, height: Int): HardwareCanvas {
        return startMethod.invoke(renderNode, width, height) as HardwareCanvas
    }

    fun end(renderNode: RenderNode, canvas: HardwareCanvas) {
        endMethod.invoke(renderNode, canvas)
    }
}