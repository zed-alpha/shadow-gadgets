package com.zedalpha.shadowgadgets.rendernode

import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.RenderNode
import android.os.Build
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.Q)
internal class RenderNodeApi29 : RenderNodeWrapper {
    private val renderNode = RenderNode("OverlayShadow")

    override fun setAlpha(alpha: Float) {
        renderNode.alpha = alpha
    }

    override fun setCameraDistance(distance: Float) {
        renderNode.cameraDistance = distance
    }

    override fun setElevation(elevation: Float) {
        renderNode.elevation = elevation
    }

    override fun setOutline(outline: Outline?) {
        renderNode.setOutline(outline)
    }

    override fun setPivotX(pivotX: Float) {
        renderNode.pivotX = pivotX
    }

    override fun setPivotY(pivotY: Float) {
        renderNode.pivotY = pivotY
    }

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) {
        renderNode.setPosition(left, top, right, bottom)
    }

    override fun setRotationX(rotationX: Float) {
        renderNode.rotationX = rotationX
    }

    override fun setRotationY(rotationY: Float) {
        renderNode.rotationY = rotationY
    }

    override fun setRotationZ(rotation: Float) {
        renderNode.rotationZ = rotation
    }

    override fun setScaleX(scaleX: Float) {
        renderNode.scaleX = scaleX
    }

    override fun setScaleY(scaleY: Float) {
        renderNode.scaleY = scaleY
    }

    override fun setTranslationX(translationX: Float) {
        renderNode.translationX = translationX
    }

    override fun setTranslationY(translationY: Float) {
        renderNode.translationY = translationY
    }

    override fun setTranslationZ(translationZ: Float) {
        renderNode.translationZ = translationZ
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRenderNode(renderNode)
    }
}