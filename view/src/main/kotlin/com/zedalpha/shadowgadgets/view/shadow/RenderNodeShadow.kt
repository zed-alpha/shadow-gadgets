package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory

internal class RenderNodeShadow : CoreShadow() {

    private val renderNode = RenderNodeFactory.create("Shadow")

    override fun dispose() {}

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
        get() = renderNode.ambientColor
        set(value) {
            renderNode.ambientColor = value
        }

    override var spotColor: Int
        get() = renderNode.spotColor
        set(value) {
            renderNode.spotColor = value
        }

    override val left: Int get() = renderNode.left

    override val top: Int get() = renderNode.top

    override val right: Int get() = renderNode.right

    override val bottom: Int get() = renderNode.bottom

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) {
        renderNode.setPosition(left, top, right, bottom)
    }

    override fun setOutline(outline: Outline) = renderNode.setOutline(outline)

    override fun hasIdentityMatrix(): Boolean = renderNode.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) = renderNode.getMatrix(outMatrix)

    override fun getInverseMatrix(outMatrix: Matrix) =
        renderNode.getInverseMatrix(outMatrix)

    override fun onDraw(canvas: Canvas) = renderNode.draw(canvas)
}