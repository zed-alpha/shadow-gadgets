package com.zedalpha.shadowgadgets.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.RenderNode
import android.os.Build
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.Q)
internal class RenderNodeApi29 : RenderNodeWrapper, RenderNodeColors {
    private val renderNode = RenderNode("OverlayShadow")

    override fun setAlpha(alpha: Float) = renderNode.setAlpha(alpha)

    override fun setCameraDistance(distance: Float) = renderNode.setCameraDistance(distance)

    override fun setElevation(elevation: Float) = renderNode.setElevation(elevation)

    override fun setOutline(outline: Outline?) = renderNode.setOutline(outline)

    override fun setPivotX(pivotX: Float) = renderNode.setPivotX(pivotX)

    override fun setPivotY(pivotY: Float) = renderNode.setPivotY(pivotY)

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        renderNode.setPosition(left, top, right, bottom)

    override fun setRotationX(rotationX: Float) = renderNode.setRotationX(rotationX)

    override fun setRotationY(rotationY: Float) = renderNode.setRotationY(rotationY)

    override fun setRotationZ(rotation: Float) = renderNode.setRotationZ(rotation)

    override fun setScaleX(scaleX: Float) = renderNode.setScaleX(scaleX)

    override fun setScaleY(scaleY: Float) = renderNode.setScaleY(scaleY)

    override fun setTranslationX(translationX: Float) = renderNode.setTranslationX(translationX)

    override fun setTranslationY(translationY: Float) = renderNode.setTranslationY(translationY)

    override fun setTranslationZ(translationZ: Float) = renderNode.setTranslationZ(translationZ)

    override fun setAmbientShadowColor(color: Int) = renderNode.setAmbientShadowColor(color)

    override fun setSpotShadowColor(color: Int) = renderNode.setSpotShadowColor(color)

    override fun hasIdentityMatrix() = renderNode.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) {
        renderNode.getMatrix(outMatrix)
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRenderNode(renderNode)
    }
}