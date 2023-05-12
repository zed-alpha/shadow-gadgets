package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clippedShadowPlane


internal class OverlayController(private val parentView: ViewGroup) :
    View.OnAttachStateChangeListener, ViewTreeObserver.OnPreDrawListener {

    private val backgroundPlane = BackgroundDrawPlane(parentView)

    private val foregroundPlane = DrawPlane(parentView)

    fun attachToParent() {
        val parent = parentView
        parent.overlayController = this
        parent.addOnAttachStateChangeListener(this)
        if (parent.isAttachedToWindow) addDrawListener()
    }

    private fun detachFromParent() {
        val parent = parentView
        parent.overlayController = null
        parent.removeOnAttachStateChangeListener(this)
        if (parent.isAttachedToWindow) removeDrawListener()
    }

    fun notifyRecyclerDetach() {
        backgroundPlane.ensureCleared()
        foregroundPlane.ensureCleared()
        detachFromParent()
    }

    fun addOverlayShadow(target: View) {
        val plane = when (target.clippedShadowPlane) {
            ClippedShadowPlane.Foreground -> foregroundPlane
            else -> backgroundPlane
        }
        OverlayShadow(target, plane, this).attachToTarget()
    }

    fun removeShadow(shadow: OverlayShadow) {
        shadow.detachFromTarget()
        if (backgroundPlane.isEmpty() && foregroundPlane.isEmpty()) {
            detachFromParent()
        }
    }

    override fun onPreDraw(): Boolean {
        backgroundPlane.checkInvalidate()
        foregroundPlane.checkInvalidate()
        return true
    }

    override fun onViewAttachedToWindow(v: View) {
        addDrawListener()
    }

    override fun onViewDetachedFromWindow(v: View) {
        removeDrawListener()
    }

    private fun addDrawListener() {
        parentView.viewTreeObserver.addOnPreDrawListener(this)
    }

    private fun removeDrawListener() {
        parentView.viewTreeObserver.removeOnPreDrawListener(this)
    }
}

internal fun getOrCreateController(parentView: ViewGroup) =
    parentView.overlayController
        ?: OverlayController(parentView).apply { attachToParent() }

internal var ViewGroup.overlayController: OverlayController?
    get() = getTag(R.id.overlay_controller) as? OverlayController
    set(value) = setTag(R.id.overlay_controller, value)