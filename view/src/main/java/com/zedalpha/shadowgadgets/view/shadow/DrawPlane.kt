package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import androidx.annotation.CallSuper


internal open class DrawPlane(
    protected val parentView: ViewGroup
) : View.OnLayoutChangeListener {

    protected val shadows = mutableListOf<OverlayShadow>()

    fun isEmpty() = shadows.isEmpty()

    fun addShadow(shadow: OverlayShadow) {
        if (shadows.isEmpty()) attachToParent()
        shadows += shadow
    }

    fun removeShadow(shadow: OverlayShadow) {
        shadows -= shadow
        if (shadows.isEmpty()) detachFromParent()
    }

    fun ensureCleared() {
        shadows.forEach { it.detachFromTarget() }
        shadows.clear()
    }

    protected val planeDrawable = object : BaseDrawable() {
        override fun draw(canvas: Canvas) {
            shadows.forEach { it.draw(canvas) }
        }
    }

    private fun attachToParent() {
        val parent = parentView
        if (parent.isLaidOut) setSize(parent.width, parent.height)
        parent.addOnLayoutChangeListener(this)
        addToOverlay(parent.overlay)
    }

    private fun detachFromParent() {
        val parent = parentView
        parent.removeOnLayoutChangeListener(this)
        removeFromOverlay(parent.overlay)
    }

    protected open fun addToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(planeDrawable)
    }

    protected open fun removeFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(planeDrawable)
    }

    fun checkInvalidate() {
        shadows.forEach { shadow ->
            if (shadow.checkInvalidate()) {
                invalidatePlane()
                return
            }
        }
    }

    @CallSuper
    protected open fun invalidatePlane() {
        parentView.invalidate()
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        setSize(right - left, bottom - top)
    }

    protected open fun setSize(width: Int, height: Int) {}
}

internal class BackgroundDrawPlane(
    parentView: ViewGroup
) : DrawPlane(parentView) {

    private val projector = createProjector(parentView.context, planeDrawable)

    override fun addToOverlay(overlay: ViewGroupOverlay) {
        projector.addToOverlay(overlay)
        if (parentView.background == null) parentView.background = EmptyDrawable
    }

    override fun removeFromOverlay(overlay: ViewGroupOverlay) {
        projector.removeFromOverlay(overlay)
        if (parentView.background == EmptyDrawable) parentView.background = null
    }

    override fun setSize(width: Int, height: Int) {
        projector.setSize(width, height)
    }

    override fun invalidatePlane() {
        super.invalidatePlane()
        projector.invalidateProjection()
    }
}

private object EmptyDrawable : BaseDrawable() {
    override fun draw(canvas: Canvas) {}
}