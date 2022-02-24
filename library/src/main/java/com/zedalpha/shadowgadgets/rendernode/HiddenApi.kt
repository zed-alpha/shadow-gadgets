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


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal open class RenderNodeApi21 : RenderNodeWrapper {
    protected val renderNode: RenderNode = RenderNode.create("OverlayShadow", null)

    override fun initialize() {
        recordEmptyDisplayList()
    }

    private fun recordEmptyDisplayList() {
        val canvas = RenderNodeReflector.start(renderNode, 0, 0)
        RenderNodeReflector.end(renderNode, canvas)
    }

    override fun setAlpha(alpha: Float) = renderNode.setAlpha(alpha)

    override fun setCameraDistance(distance: Float) = renderNode.setCameraDistance(distance)

    override fun setElevation(elevation: Float) = renderNode.setElevation(elevation)

    override fun setOutline(outline: Outline?) = renderNode.setOutline(outline)

    override fun setPivotX(pivotX: Float) = renderNode.setPivotX(pivotX)

    override fun setPivotY(pivotY: Float) = renderNode.setPivotY(pivotY)

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        renderNode.setLeftTopRightBottom(left, top, right, bottom)

    override fun setRotationX(rotationX: Float) = renderNode.setRotationX(rotationX)

    override fun setRotationY(rotationY: Float) = renderNode.setRotationY(rotationY)

    override fun setRotationZ(rotation: Float) = renderNode.setRotation(rotation)

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
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as HardwareCanvas).drawRenderNode(renderNode)
    }
}

@RequiresApi(Build.VERSION_CODES.M)
internal class RenderNodeApi23 : RenderNodeApi21() {
    override fun initialize() {
        recordEmptyDisplayList()
    }

    private fun recordEmptyDisplayList() {
        val canvas = renderNode.start(0, 0)
        renderNode.end(canvas)
    }

    override fun draw(canvas: Canvas) {
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as DisplayListCanvas).drawRenderNode(renderNode)
    }
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