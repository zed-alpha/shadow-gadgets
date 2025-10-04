package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory

internal class RenderNodeShadow : CoreShadow() {

    private val shadow = RenderNodeFactory.create("CoreShadow")

    override fun dispose() {}

    override var alpha: Float
        get() = shadow.alpha
        set(value) {
            shadow.alpha = value
        }

    override var cameraDistance: Float
        get() = shadow.cameraDistance
        set(value) {
            shadow.cameraDistance = value
        }

    override var elevation: Float
        get() = shadow.elevation
        set(value) {
            shadow.elevation = value
        }

    override var pivotX: Float
        get() = shadow.pivotX
        set(value) {
            shadow.pivotX = value
        }

    override var pivotY: Float
        get() = shadow.pivotY
        set(value) {
            shadow.pivotY = value
        }

    override var rotationX: Float
        get() = shadow.rotationX
        set(value) {
            shadow.rotationX = value
        }

    override var rotationY: Float
        get() = shadow.rotationY
        set(value) {
            shadow.rotationY = value
        }

    override var rotationZ: Float
        get() = shadow.rotationZ
        set(value) {
            shadow.rotationZ = value
        }

    override var scaleX: Float
        get() = shadow.scaleX
        set(value) {
            shadow.scaleX = value
        }

    override var scaleY: Float
        get() = shadow.scaleY
        set(value) {
            shadow.scaleY = value
        }

    override var translationX: Float
        get() = shadow.translationX
        set(value) {
            shadow.translationX = value
        }

    override var translationY: Float
        get() = shadow.translationY
        set(value) {
            shadow.translationY = value
        }

    override var translationZ: Float
        get() = shadow.translationZ
        set(value) {
            shadow.translationZ = value
        }

    override var ambientColor: Int
        get() = shadow.ambientColor
        set(value) {
            shadow.ambientColor = value
        }

    override var spotColor: Int
        get() = shadow.spotColor
        set(value) {
            shadow.spotColor = value
        }

    override val left: Int get() = shadow.left

    override val top: Int get() = shadow.top

    override val right: Int get() = shadow.right

    override val bottom: Int get() = shadow.bottom

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) {
        shadow.setPosition(left, top, right, bottom)
    }

    override fun setOutline(outline: Outline) = shadow.setOutline(outline)

    override fun hasIdentityMatrix(): Boolean = shadow.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) = shadow.getMatrix(outMatrix)

    override fun getInverseMatrix(outMatrix: Matrix) =
        shadow.getInverseMatrix(outMatrix)

    override fun onDraw(canvas: Canvas) = shadow.drawRenderNode(canvas)
}