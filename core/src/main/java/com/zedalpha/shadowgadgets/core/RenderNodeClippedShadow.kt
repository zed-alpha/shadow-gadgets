package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

class RenderNodeClippedShadow : ClippedShadow() {

    private val shadowNode = RenderNodeFactory.newInstance()

    override var alpha: Float by shadowNode::alpha

    override var cameraDistance: Float by shadowNode::cameraDistance

    override var elevation: Float by shadowNode::elevation

    override var pivotX: Float by shadowNode::pivotX

    override var pivotY: Float by shadowNode::pivotY

    override var rotationX: Float by shadowNode::rotationX

    override var rotationY: Float by shadowNode::rotationY

    override var rotationZ: Float by shadowNode::rotationZ

    override var scaleX: Float by shadowNode::scaleX

    override var scaleY: Float by shadowNode::scaleY

    override var translationX: Float by shadowNode::translationX

    override var translationY: Float by shadowNode::translationY

    override var translationZ: Float by shadowNode::translationZ

    override var ambientColor: Int by shadowNode::ambientColor

    override var spotColor: Int by shadowNode::spotColor

    override fun hasIdentityMatrix() = shadowNode.hasIdentityMatrix()

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
}