@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.RenderNode
import android.os.Build
import androidx.annotation.RequiresApi


@RequiresApi(Build.VERSION_CODES.Q)
internal class RenderNodeApi29 : RenderNodeWrapper, RenderNodeColors {
    private val renderNode = RenderNode("ShadowGadgets")

    override fun getAlpha(): Float =
        renderNode.alpha

    override fun setAlpha(alpha: Float): Boolean =
        renderNode.setAlpha(alpha)

    override fun getCameraDistance(): Float =
        renderNode.cameraDistance

    override fun setCameraDistance(distance: Float): Boolean =
        renderNode.setCameraDistance(distance)

    override fun getElevation(): Float =
        renderNode.elevation

    override fun setElevation(elevation: Float): Boolean =
        renderNode.setElevation(elevation)

    override fun getPivotX(): Float =
        renderNode.pivotX

    override fun setPivotX(pivotX: Float): Boolean =
        renderNode.setPivotX(pivotX)

    override fun getPivotY(): Float =
        renderNode.pivotY

    override fun setPivotY(pivotY: Float): Boolean =
        renderNode.setPivotY(pivotY)

    override fun getRotationX(): Float =
        renderNode.rotationX

    override fun setRotationX(rotationX: Float): Boolean =
        renderNode.setRotationX(rotationX)

    override fun getRotationY(): Float =
        renderNode.rotationY

    override fun setRotationY(rotationY: Float): Boolean =
        renderNode.setRotationY(rotationY)

    override fun getRotationZ(): Float =
        renderNode.rotationZ

    override fun setRotationZ(rotationZ: Float): Boolean =
        renderNode.setRotationZ(rotationZ)

    override fun getScaleX(): Float =
        renderNode.scaleX

    override fun setScaleX(scaleX: Float): Boolean =
        renderNode.setScaleX(scaleX)

    override fun getScaleY(): Float =
        renderNode.scaleY

    override fun setScaleY(scaleY: Float): Boolean =
        renderNode.setScaleY(scaleY)

    override fun getTranslationX(): Float =
        renderNode.translationX

    override fun setTranslationX(translationX: Float): Boolean =
        renderNode.setTranslationX(translationX)

    override fun getTranslationY(): Float =
        renderNode.translationY

    override fun setTranslationY(translationY: Float): Boolean =
        renderNode.setTranslationY(translationY)

    override fun getTranslationZ(): Float =
        renderNode.translationZ

    override fun setTranslationZ(translationZ: Float): Boolean =
        renderNode.setTranslationZ(translationZ)

    override fun getAmbientShadowColor(): Int =
        renderNode.ambientShadowColor

    override fun setAmbientShadowColor(color: Int): Boolean =
        renderNode.setAmbientShadowColor(color)

    override fun getSpotShadowColor(): Int =
        renderNode.spotShadowColor

    override fun setSpotShadowColor(color: Int): Boolean =
        renderNode.setSpotShadowColor(color)

    override fun setOutline(outline: Outline?): Boolean =
        renderNode.setOutline(outline)

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        renderNode.setPosition(left, top, right, bottom)

    override fun hasIdentityMatrix(): Boolean =
        renderNode.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) {
        renderNode.getMatrix(outMatrix)
    }

    override fun setProjectBackwards(shouldProject: Boolean): Boolean =
        renderNode.setProjectBackwards(shouldProject)

    override fun setProjectionReceiver(shouldReceive: Boolean) =
        renderNode.setProjectionReceiver(shouldReceive)

    override fun beginRecording(width: Int, height: Int): Canvas =
        renderNode.beginRecording(width, height)

    override fun endRecording(canvas: Canvas) {
        renderNode.endRecording()
    }

    override fun draw(canvas: Canvas) {
        canvas.drawRenderNode(renderNode)
    }
}