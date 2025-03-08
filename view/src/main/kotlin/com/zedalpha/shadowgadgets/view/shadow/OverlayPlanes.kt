package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import androidx.annotation.CallSuper
import androidx.core.graphics.withClip
import com.zedalpha.shadowgadgets.core.fastForEach
import com.zedalpha.shadowgadgets.view.internal.BaseDrawable
import com.zedalpha.shadowgadgets.view.internal.Projector

internal open class OverlayPlane(
    protected val parentView: ViewGroup,
    private val controller: OverlayController,
) : DrawPlane {

    protected val shadows = mutableListOf<GroupShadow>()

    protected val drawable = object : BaseDrawable() {

        override fun draw(canvas: Canvas) = with(parentView) {
            if (clipToPadding) {
                canvas.withClip(
                    paddingLeft,
                    paddingTop,
                    width - paddingRight,
                    height - paddingBottom
                ) { shadows.fastForEach { it.draw(canvas) } }
            } else {
                shadows.fastForEach { it.draw(canvas) }
            }
        }
    }

    fun attach() = attachToOverlay(parentView.overlay)

    final override fun addShadow(shadow: GroupShadow) {
        shadows.add(shadow)
        if (RequiresInvalidateOnToggle) parentView.invalidate()
    }

    final override fun removeShadow(shadow: GroupShadow) {
        shadows.remove(shadow)
        controller.disposeShadow(shadow)
        if (shadows.isEmpty()) {
            detachFromOverlay(parentView.overlay)
            controller.disposePlane(this)
            dispose()
        }

        if (RequiresInvalidateOnToggle) parentView.invalidate()
    }

    protected open fun attachToOverlay(overlay: ViewGroupOverlay) =
        overlay.add(drawable)

    protected open fun detachFromOverlay(overlay: ViewGroupOverlay) =
        overlay.remove(drawable)

    @CallSuper
    open fun setSize(width: Int, height: Int) {
        drawable.setBounds(0, 0, width, height)
    }

    @CallSuper
    open fun checkInvalidate() =
        shadows.fastForEach { shadow ->
            if (shadow.shouldInvalidate()) {
                invalidatePlane()
                return
            }
        }

    @CallSuper
    override fun invalidatePlane() = drawable.invalidateSelf()
}

internal class ProjectorOverlayPlane(
    parentView: ViewGroup,
    controller: OverlayController
) : OverlayPlane(parentView, controller) {

    private val projector = Projector(parentView.context, drawable)

    override fun attachToOverlay(overlay: ViewGroupOverlay) {
        projector.addToOverlay(overlay)
        if (parentView.background === null) {
            parentView.background = EmptyDrawable
        }
        parentView.postInvalidate()
    }

    override fun detachFromOverlay(overlay: ViewGroupOverlay) {
        projector.removeFromOverlay(overlay)
        if (parentView.background === EmptyDrawable) {
            parentView.background = null
        }
    }

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        projector.setSize(width, height)
    }

    override fun checkInvalidate() {
        super.checkInvalidate()
        projector.refresh()
    }

    override fun invalidatePlane() {
        super.invalidatePlane()
        projector.invalidateProjection()
        parentView.invalidate()
    }

    override fun dispose() = projector.dispose()
}

private object EmptyDrawable : BaseDrawable() {
    override fun draw(canvas: Canvas) {}
}