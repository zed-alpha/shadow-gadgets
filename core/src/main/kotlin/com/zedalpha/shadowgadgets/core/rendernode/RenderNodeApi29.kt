package com.zedalpha.shadowgadgets.core.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RenderNode
import androidx.annotation.RequiresApi

@RequiresApi(29)
internal class RenderNodeApi29(name: String?) : RenderNodeWrapper {

    private val renderNode = RenderNode(name)

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
        get() = renderNode.rotationZ
        set(value) {
            renderNode.rotationZ = value
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
        get() = renderNode.ambientShadowColor
        set(value) {
            renderNode.ambientShadowColor = value
        }

    override var spotColor: Int
        get() = renderNode.spotShadowColor
        set(value) {
            renderNode.spotShadowColor = value
        }

    override val left: Int get() = renderNode.left

    override val top: Int get() = renderNode.top

    override val right: Int get() = renderNode.right

    override val bottom: Int get() = renderNode.bottom

    override fun setPosition(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Boolean = renderNode.setPosition(left, top, right, bottom)

    override fun setOutline(outline: Outline?) {
        renderNode.setOutline(outline)
    }

    override fun hasIdentityMatrix(): Boolean = renderNode.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) = renderNode.getMatrix(outMatrix)

    override fun draw(canvas: Canvas) = canvas.drawRenderNode(renderNode)

    override fun setClipToBounds(clipToBounds: Boolean): Boolean =
        renderNode.setClipToBounds(clipToBounds)

    override fun setProjectBackwards(shouldProject: Boolean): Boolean =
        renderNode.setProjectBackwards(shouldProject)

    override fun setProjectionReceiver(shouldReceive: Boolean): Boolean =
        renderNode.setProjectionReceiver(shouldReceive)

    override fun beginRecording(width: Int, height: Int): Canvas =
        renderNode.beginRecording(width, height)

    override fun endRecording(canvas: Canvas) = renderNode.endRecording()

    override fun hasDisplayList(): Boolean = renderNode.hasDisplayList()

    override fun discardDisplayList() = renderNode.discardDisplayList()

    override fun setUseCompositingLayer(
        forceToLayer: Boolean,
        paint: Paint?
    ): Boolean = renderNode.setUseCompositingLayer(forceToLayer, paint)
}