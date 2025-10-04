package com.zedalpha.shadowgadgets.view.plane

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Build
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.view.internal.BaseDrawable
import com.zedalpha.shadowgadgets.view.internal.BaseView
import com.zedalpha.shadowgadgets.view.internal.fastLayout
import com.zedalpha.shadowgadgets.view.internal.viewPainter
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeReflector
import com.zedalpha.shadowgadgets.view.rendernode.nativeRenderNode
import com.zedalpha.shadowgadgets.view.rendernode.record

internal abstract class OverlayProjector(
    viewGroup: ViewGroup,
    content: (Canvas) -> Unit
) : OverlayDrawable(viewGroup, content) {

    companion object {

        fun new(
            viewGroup: ViewGroup,
            content: (Canvas) -> Unit
        ): OverlayProjector =
            when {
                Build.VERSION.SDK_INT >= 29 ->
                    RenderNodeProjector(viewGroup, content)
                RenderNodeFactory.isOpen ->
                    RenderNodeWrapperProjector(viewGroup, content)
                RenderNodeReflector.isValid ->
                    RenderNodeReflectorProjector(viewGroup, content)
                else ->
                    ViewProjector(viewGroup, content)
            }
    }

    init {
        if (viewGroup.background == null) viewGroup.background = EmptyDrawable
    }

    @CallSuper
    override fun dispose() {
        super.dispose()
        if (viewGroup.background === EmptyDrawable) viewGroup.background = null
    }

    final override fun onBoundsChange(bounds: Rect) =
        updateSize(bounds.width(), bounds.height())

    protected abstract fun updateSize(width: Int, height: Int)
}

private object EmptyDrawable : BaseDrawable() {
    override fun draw(canvas: Canvas) {}
}

@RequiresApi(29)
private class RenderNodeProjector(
    viewGroup: ViewGroup,
    content: (Canvas) -> Unit
) : OverlayProjector(viewGroup, content) {

    private val base = nativeRenderNode("ProjectorBase")

    private val projector =
        nativeRenderNode("Projector").apply { setProjectBackwards(true) }

    override fun draw(canvas: Canvas) {
        projector.record { super.draw(it) }
        base.record { it.drawRenderNode(projector) }
        canvas.drawRenderNode(base)
    }

    override fun dispose() {
        super.dispose()
        base.discardDisplayList()
        projector.discardDisplayList()
    }

    override fun updateSize(width: Int, height: Int) {
        base.setPosition(0, 0, width, height)
        projector.setPosition(0, 0, width, height)
    }
}

private class RenderNodeWrapperProjector(
    viewGroup: ViewGroup,
    content: (Canvas) -> Unit
) : OverlayProjector(viewGroup, content) {

    private val base = RenderNodeFactory.create("ProjectorBase")

    private val projector =
        RenderNodeFactory.create("Projector")
            .apply { setProjectBackwards(true) }

    override fun draw(canvas: Canvas) {
        projector.record { super.draw(it) }
        base.record { projector.drawRenderNode(it) }
        base.drawRenderNode(canvas)
    }

    override fun dispose() {
        super.dispose()
        base.discardDisplayList()
        projector.discardDisplayList()
    }

    override fun updateSize(width: Int, height: Int) {
        base.setPosition(0, 0, width, height)
        projector.setPosition(0, 0, width, height)
    }
}

private class RenderNodeReflectorProjector(
    viewGroup: ViewGroup,
    content: (Canvas) -> Unit
) : OverlayProjector(viewGroup, content) {

    private val base = RenderNodeReflector.createRenderNode("ProjectorBase")

    private val projector =
        RenderNodeReflector.createRenderNode("Projector")
            .apply { RenderNodeReflector.setProjectBackwards(this, true) }

    override fun draw(canvas: Canvas) {
        RenderNodeReflector.record(projector) { canvas ->
            super.draw(canvas)
        }
        RenderNodeReflector.record(base) { canvas ->
            RenderNodeReflector.drawRenderNode(canvas, projector)
        }
        RenderNodeReflector.drawRenderNode(canvas, base)
    }

    override fun dispose() {
        super.dispose()
        RenderNodeReflector.discardDisplayList(base)
        RenderNodeReflector.discardDisplayList(projector)
    }

    override fun updateSize(width: Int, height: Int) {
        RenderNodeReflector.setPosition(base, 0, 0, width, height)
        RenderNodeReflector.setPosition(projector, 0, 0, width, height)
    }
}

private class ViewProjector(viewGroup: ViewGroup, content: (Canvas) -> Unit) :
    OverlayProjector(viewGroup, content) {

    private val projector =
        object : BaseDrawable() {

            override fun isProjected(): Boolean = true

            @Suppress("RedundantOverride")
            override fun draw(canvas: Canvas) =
                super@ViewProjector.draw(canvas)
        }

    private val base =
        ProjectorBase(viewGroup.context).apply { background = projector }

    private val viewPainter = viewGroup.viewPainter

    init {
        viewPainter?.add(base)
    }

    override fun dispose() {
        super.dispose()
        viewPainter?.remove(base)
    }

    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) {
        base.superInvalidate()
        viewPainter?.drawView(canvas, base)
    }

    override fun updateSize(width: Int, height: Int) {
        base.fastLayout(0, 0, width, height)
        projector.superSetBounds(0, 0, width, height)
    }
}

private class ProjectorBase(context: Context) : BaseView(context)