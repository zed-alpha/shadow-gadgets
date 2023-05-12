package com.zedalpha.shadowgadgets.view.shadow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.RenderNode
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroupOverlay
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory


internal interface Projector {
    fun addToOverlay(overlay: ViewGroupOverlay)
    fun removeFromOverlay(overlay: ViewGroupOverlay)
    fun setSize(width: Int, height: Int)
    fun invalidateProjection() {}
}

internal fun createProjector(context: Context, drawable: Drawable) = when {
    Build.VERSION.SDK_INT >= 29 -> ProjectorDrawable(drawable)  // Extra safety
    RenderNodeFactory.isOpenForBusiness -> WrapperProjectorDrawable(drawable)
    ProjectorReflector.isProjectorReflectoring -> ReflectorProjectorDrawable(drawable)
    else -> ProjectorView(context, drawable)
}

@RequiresApi(29)
private class ProjectorDrawable(
    private val projectedDrawable: Drawable
) : BaseDrawable(), Projector {

    private val baseNode = RenderNode("Projector").apply {
        clipToBounds = false
    }

    private val projectedNode = RenderNode("Projector").apply {
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
}

private class WrapperProjectorDrawable(
    private val projectedDrawable: Drawable
) : BaseDrawable(), Projector {

    private val baseNode = RenderNodeFactory.newInstance().apply {
        setClipToBounds(false)
    }

    private val projectedNode = RenderNodeFactory.newInstance().apply {
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
}

private class ReflectorProjectorDrawable(
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
}

@SuppressLint("ViewConstructor")
private class ProjectorView(
    context: Context,
    private val projectedDrawable: Drawable
) : View(context), Projector {

    init {
        background = object : BaseDrawable() {
            override fun draw(canvas: Canvas) {
                projectedDrawable.draw(canvas)
            }

            override fun isProjected() = true
        }
    }

    override fun setSize(width: Int, height: Int) {
        right = width
        bottom = height
    }

    override fun addToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(this)
    }

    override fun removeFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(this)
    }

    override fun invalidateProjection() {
        invalidate()
    }
}