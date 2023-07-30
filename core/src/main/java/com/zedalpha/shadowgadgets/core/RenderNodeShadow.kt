package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

internal class RenderNodeShadow : CoreShadow() {

    private val shadowNode = RenderNodeFactory.newInstance("CoreShadow")

    override var alpha: Float
        get() = shadowNode.alpha
        set(value) {
            shadowNode.alpha = value
        }

    override var cameraDistance: Float
        get() = shadowNode.cameraDistance
        set(value) {
            shadowNode.cameraDistance = value
        }

    override var elevation: Float
        get() = shadowNode.elevation
        set(value) {
            shadowNode.elevation = value
        }

    override var pivotX: Float
        get() = shadowNode.pivotX
        set(value) {
            shadowNode.pivotX = value
        }

    override var pivotY: Float
        get() = shadowNode.pivotY
        set(value) {
            shadowNode.pivotY = value
        }

    override var rotationX: Float
        get() = shadowNode.rotationX
        set(value) {
            shadowNode.rotationX = value
        }

    override var rotationY: Float
        get() = shadowNode.rotationY
        set(value) {
            shadowNode.rotationY = value
        }

    override var rotationZ: Float
        get() = shadowNode.rotationZ
        set(value) {
            shadowNode.rotationZ = value
        }

    override var scaleX: Float
        get() = shadowNode.scaleX
        set(value) {
            shadowNode.scaleX = value
        }

    override var scaleY: Float
        get() = shadowNode.scaleY
        set(value) {
            shadowNode.scaleY = value
        }

    override var translationX: Float
        get() = shadowNode.translationX
        set(value) {
            shadowNode.translationX = value
        }

    override var translationY: Float
        get() = shadowNode.translationY
        set(value) {
            shadowNode.translationY = value
        }

    override var translationZ: Float
        get() = shadowNode.translationZ
        set(value) {
            shadowNode.translationZ = value
        }

    override var ambientColor: Int
        get() = shadowNode.ambientColor
        set(value) {
            shadowNode.ambientColor = value
        }

    override var spotColor: Int
        get() = shadowNode.spotColor
        set(value) {
            shadowNode.spotColor = value
        }

    override fun hasIdentityMatrix(): Boolean =
        shadowNode.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) {
        shadowNode.getMatrix(outMatrix)
    }

    override fun setOutline(outline: Outline?) {
        super.setOutline(outline)
        shadowNode.setOutline(outline)
    }

    override fun onDraw(canvas: Canvas) {
        shadowNode.draw(canvas)
    }

    override fun dispose() {}
}