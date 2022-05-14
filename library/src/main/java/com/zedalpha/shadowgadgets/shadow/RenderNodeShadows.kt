@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.clippedShadowPlane
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory


internal class RenderNodeShadowContainer(
    parentView: ViewGroup,
    controller: ShadowController
) : ShadowContainer<RenderNodeShadow, RenderNodeShadowPlane>(parentView, controller) {
    override val backgroundPlane = DrawablePlane()
    private val overlayProjector = OverlayProjectorDrawable(backgroundPlane)

    override val foregroundPlane = DrawablePlane()

    override fun attachToParent() {
        val parent = parentView
        parent.overlay.add(overlayProjector)
        if (parent.isLaidOut) overlayProjector.setSize(parent.width, parent.height)
        if (parent.background == null) parent.background = EmptyDrawable

        parent.overlay.add(foregroundPlane)
    }

    override fun detachFromParent() {
        val parent = parentView
        parent.overlay.remove(overlayProjector)
        if (parent.background == EmptyDrawable) parent.background = null

        parent.overlay.remove(foregroundPlane)
    }

    override fun createShadow(targetView: View, plane: RenderNodeShadowPlane) =
        RenderNodeShadow(targetView, controller, plane)

    override fun determinePlane(targetView: View) =
        targetView.clippedShadowPlane

    override fun setSize(width: Int, height: Int) {
        overlayProjector.setSize(width, height)
    }

    inner class DrawablePlane : RenderNodeShadowPlane() {
        override fun updateAndInvalidateShadows() {
            var invalidate = false
            shadows.forEach { if (it.update()) invalidate = true }
            if (invalidate) parentView.invalidate()
        }
    }
}


internal sealed class RenderNodeShadowPlane : BaseDrawable(), Plane<RenderNodeShadow> {
    override val shadows = mutableListOf<RenderNodeShadow>()

    override fun draw(canvas: Canvas) {
        shadows.forEach { it.draw(canvas) }
    }
}


internal class RenderNodeShadow(
    targetView: View,
    controller: ShadowController,
    plane: RenderNodeShadowPlane
) : Shadow(targetView, controller, plane) {
    private val renderNode = RenderNodeFactory.newInstance()

    override fun setOutline(outline: Outline) {
        super.setOutline(outline)
        renderNode.setOutline(outline)
    }

    override fun update(): Boolean {
        val node = renderNode
        val target = targetView

        val left = target.left
        val top = target.top
        val right = target.right
        val bottom = target.bottom

        node.setAlpha(target.alpha)
        node.setCameraDistance(target.cameraDistance)
        node.setElevation(target.elevation)
        node.setRotationX(target.rotationX)
        node.setRotationY(target.rotationY)
        node.setRotationZ(target.rotation)
        node.setTranslationZ(target.translationZ)

        val colorsChanged = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ShadowColorsHelper.changeColors(node, target)
        } else {
            false
        }

        val areaChanged = node.setPivotX(target.pivotX) or
                node.setPivotY(target.pivotY) or
                node.setPosition(left, top, right, bottom) or
                node.setScaleX(target.scaleX) or
                node.setScaleY(target.scaleY) or
                node.setTranslationX(target.translationX) or
                node.setTranslationY(target.translationY)

        return colorsChanged || areaChanged
    }

    fun draw(canvas: Canvas) {
        if (willDraw) {
            update()

            val path = CachePath
            path.set(clipPath)

            val node = renderNode
            if (!node.hasIdentityMatrix()) {
                val matrix = CacheMatrix
                node.getMatrix(matrix)
                path.transform(matrix)
            }

            val target = targetView
            path.offset(target.left.toFloat(), target.top.toFloat())
            clipAndDraw(canvas, path, renderNode)
        }
    }
}


private class OverlayProjectorDrawable(private val projectedDrawable: Drawable) : BaseDrawable() {
    private val baseNode = RenderNodeFactory.newInstance().apply {
        setClipToBounds(false)
    }

    private val projectorNode = RenderNodeFactory.newInstance().apply {
        setClipToBounds(false)
        setProjectBackwards(true)
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        val baseCanvas = baseNode.beginRecording(width, height)
        try {
            val projectorCanvas = projectorNode.beginRecording(width, height)
            try {
                projectedDrawable.draw(projectorCanvas)
            } finally {
                projectorNode.endRecording(projectorCanvas)
            }
            projectorNode.draw(baseCanvas)
        } finally {
            baseNode.endRecording(baseCanvas)
        }
        baseNode.draw(canvas)
    }

    fun setSize(width: Int, height: Int) {
        setBounds(0, 0, width, height)
        baseNode.setPosition(0, 0, width, height)
        projectorNode.setPosition(0, 0, width, height)
    }
}


private val CacheMatrix = Matrix()