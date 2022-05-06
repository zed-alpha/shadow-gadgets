@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import android.view.DisplayListCanvas
import android.view.HardwareCanvas
import android.view.RenderNode
import androidx.annotation.RequiresApi
import java.lang.reflect.Method


internal open class RenderNodeApi21 : RenderNodeWrapper {
    protected val renderNode: RenderNode = RenderNode.create("ShadowGadgets", null)

    private fun recordEmptyDisplayList() {
        val canvas = RenderNodeReflector.start(renderNode, 0, 0)
        RenderNodeReflector.end(renderNode, canvas)
    }

    override fun getAlpha(): Float = renderNode.alpha

    override fun setAlpha(alpha: Float): Boolean = renderNode.setAlpha(alpha)

    override fun getCameraDistance(): Float = renderNode.cameraDistance

    override fun setCameraDistance(distance: Float): Boolean =
        renderNode.setCameraDistance(distance)

    override fun getElevation(): Float = renderNode.elevation

    override fun setElevation(elevation: Float): Boolean = renderNode.setElevation(elevation)

    override fun getPivotX(): Float = renderNode.pivotX

    override fun setPivotX(pivotX: Float): Boolean = renderNode.setPivotX(pivotX)

    override fun getPivotY(): Float = renderNode.pivotY

    override fun setPivotY(pivotY: Float): Boolean = renderNode.setPivotY(pivotY)

    override fun getRotationX(): Float = renderNode.rotationX

    override fun setRotationX(rotationX: Float): Boolean = renderNode.setRotationX(rotationX)

    override fun getRotationY(): Float = renderNode.rotationY

    override fun setRotationY(rotationY: Float): Boolean = renderNode.setRotationY(rotationY)

    override fun getRotationZ(): Float = renderNode.rotation

    override fun setRotationZ(rotationZ: Float): Boolean = renderNode.setRotation(rotationZ)

    override fun getScaleX(): Float = renderNode.scaleX

    override fun setScaleX(scaleX: Float): Boolean = renderNode.setScaleX(scaleX)

    override fun getScaleY(): Float = renderNode.scaleY

    override fun setScaleY(scaleY: Float): Boolean = renderNode.setScaleY(scaleY)

    override fun getTranslationX(): Float = renderNode.translationX

    override fun setTranslationX(translationX: Float): Boolean =
        renderNode.setTranslationX(translationX)

    override fun getTranslationY(): Float = renderNode.translationY

    override fun setTranslationY(translationY: Float): Boolean =
        renderNode.setTranslationY(translationY)

    override fun getTranslationZ(): Float = renderNode.translationZ

    override fun setTranslationZ(translationZ: Float): Boolean =
        renderNode.setTranslationZ(translationZ)

    override fun setOutline(outline: Outline?) = renderNode.setOutline(outline)

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        renderNode.setLeftTopRightBottom(left, top, right, bottom)

    override fun hasIdentityMatrix() = renderNode.hasIdentityMatrix()

    override fun getMatrix(outMatrix: Matrix) {
        renderNode.getMatrix(outMatrix)
    }

    override fun setProjectBackwards(shouldProject: Boolean): Boolean =
        renderNode.setProjectBackwards(shouldProject)

    override fun setProjectionReceiver(shouldReceive: Boolean): Boolean =
        renderNode.setProjectionReceiver(shouldReceive)

    override fun beginRecording(width: Int, height: Int): Canvas =
        RenderNodeReflector.start(renderNode, width, height)

    override fun endRecording(canvas: Canvas) {
        RenderNodeReflector.end(renderNode, canvas as HardwareCanvas)
    }

    override fun draw(canvas: Canvas) {
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as HardwareCanvas).drawRenderNode(renderNode)
    }
}

@RequiresApi(Build.VERSION_CODES.M)
internal open class RenderNodeApi23 : RenderNodeApi21() {
    private fun recordEmptyDisplayList() {
        val canvas = renderNode.start(0, 0)
        renderNode.end(canvas)
    }

    override fun beginRecording(width: Int, height: Int): Canvas =
        renderNode.start(0, 0)

    override fun endRecording(canvas: Canvas) {
        renderNode.end(canvas as DisplayListCanvas)
    }

    override fun draw(canvas: Canvas) {
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as DisplayListCanvas).drawRenderNode(renderNode)
    }
}

@RequiresApi(Build.VERSION_CODES.P)
internal class RenderNodeApi28 : RenderNodeApi23(), RenderNodeColors {
    override fun getAmbientShadowColor() = renderNode.ambientShadowColor

    override fun setAmbientShadowColor(color: Int) = renderNode.setAmbientShadowColor(color)

    override fun getSpotShadowColor() = renderNode.spotShadowColor

    override fun setSpotShadowColor(color: Int) = renderNode.setSpotShadowColor(color)
}

internal object RenderNodeReflector {
    private val startMethod: Method by lazy {
        RenderNode::class.java.getDeclaredMethod(
            "start",
            Int::class.java,
            Int::class.java
        )
    }

    private val endMethod: Method by lazy {
        RenderNode::class.java.getDeclaredMethod(
            "end",
            HardwareCanvas::class.java
        )
    }

    fun start(renderNode: RenderNode, width: Int, height: Int): HardwareCanvas {
        return startMethod.invoke(renderNode, width, height) as HardwareCanvas
    }

    fun end(renderNode: RenderNode, canvas: HardwareCanvas) {
        endMethod.invoke(renderNode, canvas)
    }
}