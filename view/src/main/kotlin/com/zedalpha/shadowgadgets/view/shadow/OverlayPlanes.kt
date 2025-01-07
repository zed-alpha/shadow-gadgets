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

    private val layers = OverlayLayerSet(parentView)

    protected val shadows = mutableListOf<GroupShadow>()

    protected val drawable = object : BaseDrawable() {

        override fun draw(canvas: Canvas) = with(parentView) {
            if (clipToPadding) {
                canvas.withClip(
                    paddingLeft,
                    paddingTop,
                    width - paddingRight,
                    height - paddingBottom,
                    layers::draw
                )
            } else {
                layers.draw(canvas)
            }
        }
    }

    fun requiresTracking() = layers.requiresTracking()

    fun attach() = attachToOverlay(parentView.overlay)

    final override fun addShadow(shadow: GroupShadow, color: Int) {
        shadows.add(shadow)
        layers.addShadow(shadow, color)

        if (RequiresSwitchInvalidation) parentView.invalidate()
    }

    final override fun removeShadow(shadow: GroupShadow) {
        shadows.remove(shadow)
        layers.removeShadow(shadow)
        controller.disposeShadow(shadow)
        if (shadows.isEmpty()) {
            detachFromOverlay(parentView.overlay)
            controller.disposePlane(this)
            dispose()
        }

        if (RequiresSwitchInvalidation) parentView.invalidate()
    }

    final override fun updateColor(shadow: GroupShadow, color: Int) =
        layers.updateColor(shadow, color)

    protected open fun attachToOverlay(overlay: ViewGroupOverlay) =
        overlay.add(drawable)

    protected open fun detachFromOverlay(overlay: ViewGroupOverlay) =
        overlay.remove(drawable)

    @CallSuper
    open fun setSize(width: Int, height: Int) {
        drawable.setBounds(0, 0, width, height)
        layers.setSize(width, height)
    }

    @CallSuper
    open fun checkInvalidate() =
        shadows.fastForEach { shadow ->
            if (shadow.checkInvalidate()) {
                invalidatePlane()
                layers.refresh()
                return
            }
        }

    @CallSuper
    override fun invalidatePlane() {
        layers.invalidate()
        drawable.invalidateSelf()
    }

    fun recreateLayers() {
        layers.recreate()
        drawable.invalidateSelf()
    }

    @CallSuper
    override fun dispose() = layers.dispose()
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
        if (shadows.isNotEmpty()) projector.refresh()
    }

    override fun invalidatePlane() {
        super.invalidatePlane()
        projector.invalidateProjection()
    }

    override fun dispose() {
        super.dispose()
        projector.dispose()
    }
}

private object EmptyDrawable : BaseDrawable() {
    override fun draw(canvas: Canvas) {}
}