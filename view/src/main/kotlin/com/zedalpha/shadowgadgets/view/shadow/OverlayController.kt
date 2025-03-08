package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.shadowPlane

internal class OverlayController(parentView: ViewGroup) :
    GroupController(parentView) {

    private var foregroundPlane: OverlayPlane? = null

    private var backgroundPlane: ProjectorOverlayPlane? = null

    init {
        parentView.overlayController = this
    }

    override fun detachFromParent() {
        super.detachFromParent()
        parentView.overlayController = null
        foregroundPlane?.dispose()
        backgroundPlane?.dispose()
    }

    override fun providePlane(target: View): OverlayPlane =
        if (target.clipOutlineShadow && target.shadowPlane == Foreground) {
            getOrCreateForegroundPlane()
        } else {
            getOrCreateBackgroundPlane()
        }

    private fun getOrCreateForegroundPlane() =
        foregroundPlane ?: OverlayPlane(parentView, this).apply {
            setSize(parentView.width, parentView.height)
            attach()
            foregroundPlane = this
        }

    private fun getOrCreateBackgroundPlane() =
        backgroundPlane ?: ProjectorOverlayPlane(parentView, this).apply {
            setSize(parentView.width, parentView.height)
            attach()
            backgroundPlane = this
        }

    fun disposePlane(plane: OverlayPlane) =
        if (plane == foregroundPlane) {
            foregroundPlane = null
        } else {
            backgroundPlane = null
        }

    override fun onSizeChanged(width: Int, height: Int) {
        super.onSizeChanged(width, height)
        foregroundPlane?.setSize(width, height)
        backgroundPlane?.setSize(width, height)
    }

    override fun checkInvalidate() {
        super.checkInvalidate()
        foregroundPlane?.checkInvalidate()
        backgroundPlane?.checkInvalidate()
    }

    override fun onEmpty() = detachFromParent()
}

internal fun ViewGroup.getOrCreateOverlayController() =
    overlayController ?: OverlayController(this)

private var ViewGroup.overlayController: GroupController?
    get() = getTag(R.id.overlay_controller) as? GroupController
    set(value) = setTag(R.id.overlay_controller, value)