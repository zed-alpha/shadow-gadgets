package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clippedShadowPlane


internal class OverlayController(private val parentView: ViewGroup) :
    ViewTreeObserver.OnPreDrawListener,
    View.OnAttachStateChangeListener,
    View.OnLayoutChangeListener {

    private val controllerShadows = mutableListOf<OverlayShadow>()

    private val backgroundPlane = BackgroundDrawPlane(parentView)

    private val foregroundPlane = DrawPlane(parentView)

    init {
        val parent = parentView
        parent.overlayController = this

        parent.addOnAttachStateChangeListener(this)
        if (parent.isAttachedToWindow) addDrawListener()

        parent.addOnLayoutChangeListener(this)
        if (parent.isLaidOut) setSize(parent.width, parent.height)
    }

    private fun detachFromParent() {
        val parent = parentView
        parent.overlayController = null

        parent.removeOnAttachStateChangeListener(this)
        if (parent.isAttachedToWindow) removeDrawListener()

        parent.removeOnLayoutChangeListener(this)
    }

    fun notifyRecyclerDetached() {
        // Must copy the list because notifyDetach() modifies it
        controllerShadows.toList().forEach { it.notifyDetach() }
    }

    fun addOverlayShadow(target: View) {
        val plane = when (target.clippedShadowPlane) {
            ClippedShadowPlane.Foreground -> foregroundPlane
            else -> backgroundPlane
        }
        controllerShadows += OverlayShadow(target, plane, this)
    }

    fun removeOverlayShadow(shadow: OverlayShadow) {
        controllerShadows -= shadow
        if (controllerShadows.isEmpty()) detachFromParent()
    }

    override fun onViewAttachedToWindow(view: View) {
        addDrawListener()
    }

    override fun onViewDetachedFromWindow(view: View) {
        removeDrawListener()
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

    private fun setSize(width: Int, height: Int) {
        backgroundPlane.setSize(width, height)
        foregroundPlane.setSize(width, height)
    }

    private fun addDrawListener() {
        parentView.viewTreeObserver.addOnPreDrawListener(this)
    }

    private fun removeDrawListener() {
        parentView.viewTreeObserver.removeOnPreDrawListener(this)
    }

    override fun onPreDraw(): Boolean {
        backgroundPlane.checkInvalidate()
        foregroundPlane.checkInvalidate()
        return true
    }
}

internal var ViewGroup.overlayController: OverlayController?
    get() = getTag(R.id.overlay_controller) as? OverlayController
    private set(value) = setTag(R.id.overlay_controller, value)