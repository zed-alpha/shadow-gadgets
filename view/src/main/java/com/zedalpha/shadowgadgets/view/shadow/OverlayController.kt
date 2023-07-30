package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.clippedShadowPlane


internal class OverlayController(parentView: ViewGroup) :
    ShadowController(parentView) {

    private val backgroundPlane = BackgroundOverlayPlane(parentView)

    private val foregroundPlane = OverlayPlane(parentView)

    private val layoutListener =
        View.OnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
            setSize(right - left, bottom - top)
        }

    init {
        parentView.overlayController = this
        parentView.addOnLayoutChangeListener(layoutListener)
        if (parentView.isLaidOut) setSize(parentView.width, parentView.height)
    }

    override fun detachFromParent() {
        super.detachFromParent()
        parentView.overlayController = null
        parentView.removeOnLayoutChangeListener(layoutListener)
    }

    override fun createShadow(target: View) = GroupShadow(
        target,
        this,
        if (target.run { clipOutlineShadow && clippedShadowPlane == Foreground }) {
            foregroundPlane
        } else {
            backgroundPlane
        }
    )

    override fun onPreDraw() {
        backgroundPlane.checkInvalidate()
        foregroundPlane.checkInvalidate()
    }

    override fun onEmpty() {
        detachFromParent()
    }

    private fun setSize(width: Int, height: Int) {
        backgroundPlane.setSize(width, height)
        foregroundPlane.setSize(width, height)
    }
}

internal fun ViewGroup.getOrCreateOverlayController() =
    overlayController ?: OverlayController(this)

internal var ViewGroup.overlayController: ShadowController?
    get() = getTag(R.id.overlay_controller) as? ShadowController
    private set(value) = setTag(R.id.overlay_controller, value)