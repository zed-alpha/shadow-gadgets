package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import com.zedalpha.shadowgadgets.view.viewgroup.isRecyclingViewGroupChild

internal object ShadowSwitch : View.OnAttachStateChangeListener {

    fun notifyClipChanged(view: View, turnOn: Boolean) {
        if (turnOn) {
            view.addOnAttachStateChangeListener(this)
            if (view.isAttachedToWindow) onAttached(view)
        } else {
            view.removeOnAttachStateChangeListener(this)
            if (view.isAttachedToWindow) onDetached(view)
            view.shadow?.notifyDetach()
        }
    }

    private fun onAttached(view: View) {
        val shadow = view.shadow
        if (shadow is OverlayShadow && view.isRecyclingViewGroupChild) {
            shadow.show()
        } else if (view.outlineProvider != null) {
            createShadowForView(view)
        }
    }

    private fun onDetached(view: View) {
        val shadow = view.shadow ?: return
        if (shadow is OverlayShadow && view.isRecyclingViewGroupChild) {
            shadow.hide()
        } else {
            shadow.notifyDetach()
        }
    }

    override fun onViewAttachedToWindow(view: View) {
        onAttached(view)
    }

    override fun onViewDetachedFromWindow(view: View) {
        onDetached(view)
    }
}