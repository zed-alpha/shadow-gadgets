package com.zedalpha.shadowgadgets.core.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.view.DisplayListCanvas
import android.view.HardwareCanvas
import android.view.RenderNode
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt


internal open class RenderNodeApi21(name: String?) : RenderNodeWrapper {

    protected val renderNode = RenderNode.create(name, null)

    override var alpha: Float
        get() = renderNode.alpha
        set(value) {
            renderNode.alpha = value
        }

    override var cameraDistance: Float
        get() = renderNode.cameraDistance
        set(value) {
            renderNode.cameraDistance = value
        }

    override var elevation: Float
        get() = renderNode.elevation
        set(value) {
            renderNode.elevation = value
        }

    override var pivotX: Float
        get() = renderNode.pivotX
        set(value) {
            renderNode.pivotX = value
        }

    override var pivotY: Float
        get() = renderNode.pivotY
        set(value) {
            renderNode.pivotY = value
        }

    override var rotationX: Float
        get() = renderNode.rotationX
        set(value) {
            renderNode.rotationX = value
        }

    override var rotationY: Float
        get() = renderNode.rotationY
        set(value) {
            renderNode.rotationY = value
        }

    override var rotationZ: Float
        get() = renderNode.rotation
        set(value) {
            renderNode.rotation = value
        }

    override var scaleX: Float
        get() = renderNode.scaleX
        set(value) {
            renderNode.scaleX = value
        }

    override var scaleY: Float
        get() = renderNode.scaleY
        set(value) {
            renderNode.scaleY = value
        }

    override var translationX: Float
        get() = renderNode.translationX
        set(value) {
            renderNode.translationX = value
        }

    override var translationY: Float
        get() = renderNode.translationY
        set(value) {
            renderNode.translationY = value
        }

    override var translationZ: Float
        get() = renderNode.translationZ
        set(value) {
            renderNode.translationZ = value
        }

    override var ambientColor: Int
        get() = DefaultShadowColorInt
        set(_) {}

    override var spotColor: Int
        get() = DefaultShadowColorInt
        set(_) {}

    override fun hasIdentityMatrix(): Boolean =
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

    override fun setClipToBounds(clipToBounds: Boolean): Boolean =
        renderNode.setClipToBounds(clipToBounds)

    override fun setProjectBackwards(shouldProject: Boolean): Boolean =
        renderNode.setProjectBackwards(shouldProject)

    override fun setProjectionReceiver(shouldReceive: Boolean): Boolean =
        renderNode.setProjectionReceiver(shouldReceive)

    override fun setPosition(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Boolean = renderNode.setLeftTopRightBottom(left, top, right, bottom)

    override fun beginRecording(width: Int, height: Int): Canvas =
        RenderNodeReflector.start(renderNode, width, height)

    override fun endRecording(canvas: Canvas) {
        RenderNodeReflector.end(renderNode, canvas as HardwareCanvas)
    }

    override fun hasDisplayList(): Boolean = renderNode.isValid

    private fun recordEmptyDisplayList() {
        val canvas = RenderNodeReflector.start(renderNode, 0, 0)
        RenderNodeReflector.end(renderNode, canvas)
    }
}

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

    fun start(renderNode: RenderNode, width: Int, height: Int): HardwareCanvas =
        startMethod.invoke(renderNode, width, height) as HardwareCanvas

    fun end(renderNode: RenderNode, canvas: HardwareCanvas) {
        endMethod.invoke(renderNode, canvas)
    }
}