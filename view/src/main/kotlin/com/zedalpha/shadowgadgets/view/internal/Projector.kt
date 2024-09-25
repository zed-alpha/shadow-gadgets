package com.zedalpha.shadowgadgets.view.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

internal fun Projector(context: Context, drawable: Drawable) = when {
    Build.VERSION.SDK_INT >= 29 -> DrawableProjector(drawable)  // Extra safety
    RenderNodeFactory.isOpen -> WrapperDrawableProjector(drawable)
    ProjectorReflector.isValid -> ReflectorDrawableProjector(drawable)
    else -> ViewProjector(context, drawable)
}

internal interface Projector {

    fun addToOverlay(overlay: ViewGroupOverlay)

    fun removeFromOverlay(overlay: ViewGroupOverlay)

    fun setSize(width: Int, height: Int)

    fun invalidateProjection() {}

    fun refresh() {}

    fun dispose() {}
}

@RequiresApi(29)
private class DrawableProjector(
    private val projectedDrawable: Drawable
) : BaseDrawable(), Projector {

    private val baseNode = RenderNode("Projector").apply {
        clipToBounds = false
    }

    private val projectedNode = RenderNode("Projected").apply {
        clipToBounds = false
        setProjectBackwards(true)
    }

    override fun addToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(this)
    }

    override fun removeFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(this)
    }

    override fun setSize(width: Int, height: Int) {
        setBounds(0, 0, width, height)
        baseNode.setPosition(0, 0, width, height)
        projectedNode.setPosition(0, 0, width, height)
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()

        val projectorCanvas = projectedNode.beginRecording(width, height)
        try {
            projectedDrawable.draw(projectorCanvas)
        } finally {
            projectedNode.endRecording()
        }
        val baseCanvas = baseNode.beginRecording(width, height)
        try {
            baseCanvas.drawRenderNode(projectedNode)
        } finally {
            baseNode.endRecording()
        }
        canvas.drawRenderNode(baseNode)
    }

    override fun dispose() {
        baseNode.discardDisplayList()
        projectedNode.discardDisplayList()
    }
}

private class WrapperDrawableProjector(
    private val projectedDrawable: Drawable
) : BaseDrawable(), Projector {

    private val baseNode =
        RenderNodeFactory.newInstance("Projector").apply {
            setClipToBounds(false)
        }

    private val projectedNode =
        RenderNodeFactory.newInstance("Projected").apply {
            setClipToBounds(false)
            setProjectBackwards(true)
        }

    override fun addToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(this)
    }

    override fun removeFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(this)
    }

    override fun setSize(width: Int, height: Int) {
        setBounds(0, 0, width, height)
        baseNode.setPosition(0, 0, width, height)
        projectedNode.setPosition(0, 0, width, height)
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()

        val projectorCanvas = projectedNode.beginRecording(width, height)
        try {
            projectedDrawable.draw(projectorCanvas)
        } finally {
            projectedNode.endRecording(projectorCanvas)
        }
        val baseCanvas = baseNode.beginRecording(width, height)
        try {
            projectedNode.draw(baseCanvas)
        } finally {
            baseNode.endRecording(baseCanvas)
        }
        baseNode.draw(canvas)
    }

    override fun dispose() {
        baseNode.discardDisplayList()
        projectedNode.discardDisplayList()
    }
}

private class ReflectorDrawableProjector(
    private val projectedDrawable: Drawable
) : BaseDrawable(), Projector {

    private val baseNode = ProjectorReflector.createRenderNode().apply {
        ProjectorReflector.setClipToBounds(this, false)
    }

    private val projectedNode = ProjectorReflector.createRenderNode().apply {
        ProjectorReflector.setClipToBounds(this, false)
        ProjectorReflector.setProjectBackwards(this, true)
    }

    override fun addToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(this)
    }

    override fun removeFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(this)
    }

    override fun setSize(width: Int, height: Int) {
        setBounds(0, 0, width, height)
        ProjectorReflector.setPosition(baseNode, 0, 0, width, height)
        ProjectorReflector.setPosition(projectedNode, 0, 0, width, height)
    }

    override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()

        val projectorCanvas =
            ProjectorReflector.start(projectedNode, width, height)
        try {
            projectedDrawable.draw(projectorCanvas)
        } finally {
            ProjectorReflector.end(projectedNode, projectorCanvas)
        }
        val baseCanvas = ProjectorReflector.start(baseNode, width, height)
        try {
            ProjectorReflector.drawRenderNode(baseCanvas, projectedNode)
        } finally {
            ProjectorReflector.end(baseNode, baseCanvas)
        }
        ProjectorReflector.drawRenderNode(canvas, baseNode)
    }

    override fun dispose() {
        ProjectorReflector.discardDisplayList(baseNode)
        ProjectorReflector.discardDisplayList(projectedNode)
    }
}

@SuppressLint("ViewConstructor")
private class ViewProjector(
    context: Context,
    projectedDrawable: Drawable
) : ViewGroup(context), Projector {

    private val projectedChild = View(context).apply {
        background = projectedDrawable
        visibility = View.GONE
    }

    init {
        addView(projectedChild, EmptyLayoutParams)
        background = object : BaseDrawable() {

            override fun draw(canvas: Canvas) {
                drawChild(canvas, projectedChild, 0L)
            }

            override fun isProjected(): Boolean = true
        }
    }

    override fun addToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(this)
    }

    override fun removeFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(this)
    }

    override fun setSize(width: Int, height: Int) {
        layout(0, 0, width, height)
        projectedChild.layout(0, 0, width, height)
    }

    override fun invalidateProjection() {
        invalidate()
    }

    override fun refresh() {
        detachViewFromParent(projectedChild)
        attachViewToParent(projectedChild, 0, EmptyLayoutParams)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}

private val EmptyLayoutParams = ViewGroup.LayoutParams(0, 0)