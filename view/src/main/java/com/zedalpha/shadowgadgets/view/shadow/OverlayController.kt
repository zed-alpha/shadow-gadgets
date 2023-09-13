package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.shadowPlane


internal class OverlayController(parentView: ViewGroup) :
    ShadowController(parentView) {

    private var foregroundPlane: OverlayPlane? = null

    private var backgroundPlane: BackgroundOverlayPlane? = null

    private val layoutListener =
        View.OnLayoutChangeListener { _, l, t, r, b, _, _, _, _ ->
            setSize(r - l, b - t)
        }

    init {
        parentView.overlayController = this
        parentView.addOnLayoutChangeListener(layoutListener)
        parentView.run { if (width > 0 && height > 0) setSize(width, height) }
    }

    override fun detachFromParent() {
        super.detachFromParent()
        parentView.overlayController = null
        parentView.removeOnLayoutChangeListener(layoutListener)
        foregroundPlane?.dispose()
        backgroundPlane?.dispose()
    }

    override fun providePlane(target: View) = target.run {
        if (clipOutlineShadow && shadowPlane == Foreground) {
            getOrCreateForegroundPlane()
        } else {
            getOrCreateBackgroundPlane()
        }
    }

    private fun getOrCreateForegroundPlane() =
        foregroundPlane ?: OverlayPlane(parentView, this).apply {
            setSize(parentView.width, parentView.height)
            attach()
            foregroundPlane = this
        }

    private fun getOrCreateBackgroundPlane() =
        backgroundPlane ?: BackgroundOverlayPlane(parentView, this).apply {
            setSize(parentView.width, parentView.height)
            attach()
            backgroundPlane = this
        }

    fun disposePlane(plane: OverlayPlane) {
        if (plane == foregroundPlane) {
            foregroundPlane = null
        } else {
            backgroundPlane = null
        }
    }

    override fun requiresTracking(): Boolean =
        foregroundPlane?.requiresTracking() == true ||
                backgroundPlane?.requiresTracking() == true

    override fun onLocationChanged() {
        foregroundPlane?.recreateLayers()
        backgroundPlane?.recreateLayers()
    }

    override fun checkInvalidate() {
        foregroundPlane?.checkInvalidate()
        backgroundPlane?.checkInvalidate()
    }

    override fun onEmpty() {
        detachFromParent()
    }

    private fun setSize(width: Int, height: Int) {
        foregroundPlane?.setSize(width, height)
        backgroundPlane?.setSize(width, height)
    }
}

internal fun ViewGroup.getOrCreateOverlayController() =
    overlayController ?: OverlayController(this)

internal var ViewGroup.overlayController: ShadowController?
    get() = getTag(R.id.overlay_controller) as? ShadowController
    private set(value) = setTag(R.id.overlay_controller, value)