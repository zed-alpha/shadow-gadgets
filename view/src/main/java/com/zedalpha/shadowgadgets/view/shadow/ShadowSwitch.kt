package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.clippedShadowPlane
import com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsViewGroup
import com.zedalpha.shadowgadgets.view.viewgroup.inlineController
import com.zedalpha.shadowgadgets.view.viewgroup.isRecyclingViewGroupChild


internal object ShadowSwitch : View.OnAttachStateChangeListener {

    fun notifyClipChanged(view: View, turnOn: Boolean) {
        if (turnOn) {
            view.addOnAttachStateChangeListener(this)
            if (view.isAttachedToWindow) onAttached(view)
        } else {
            view.removeOnAttachStateChangeListener(this)
            if (view.isAttachedToWindow) onDetached(view)
            view.clippedShadow?.detachFromTarget()
        }
    }

    private fun onAttached(view: View) {
        val shadow = view.clippedShadow
        if (shadow == null) {
            view.createClippedShadow()
        } else if (view.isRecyclingViewGroupChild) {
            shadow.show()
        }
    }

    private fun onDetached(view: View) {
        val shadow = view.clippedShadow ?: return
        if (view.isRecyclingViewGroupChild) {
            shadow.hide()
        } else {
            shadow.detachFromTarget()
        }
    }

    override fun onViewAttachedToWindow(view: View) {
        onAttached(view)
    }

    override fun onViewDetachedFromWindow(view: View) {
        onDetached(view)
    }
}

private fun View.createClippedShadow() {
    if (outlineProvider == null) return
    val parent = parent as? ViewGroup ?: return
    if (clippedShadowPlane == ClippedShadowPlane.Inline) {
        if (parent is ClippedShadowsViewGroup) {
            parent.inlineController.createGroupShadow(this)
        } else {
            IndependentShadow(this)
        }
    } else {
        parent.getOrCreateOverlayController().createGroupShadow(this)
    }
}

internal fun View.recreateClippedShadow() {
    clippedShadow?.detachFromTarget() ?: return
    createClippedShadow()
}