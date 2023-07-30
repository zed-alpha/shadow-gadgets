package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.clippedShadowPlane
import com.zedalpha.shadowgadgets.view.requiresColor
import com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsViewGroup
import com.zedalpha.shadowgadgets.view.viewgroup.inlineController
import com.zedalpha.shadowgadgets.view.viewgroup.isRecyclingViewGroupChild


internal object ShadowSwitch : View.OnAttachStateChangeListener {

    fun notifyPropertyChanged(view: View) = with(view) {
        if (clipOutlineShadow || requiresColor) {
            if (isWatched) {
                if (shadow?.checkRecreate() == true) recreateShadow()
            } else {
                isWatched = true
                addOnAttachStateChangeListener(this@ShadowSwitch)
                if (isAttachedToWindow) onAttached(view)
            }
        } else if (isWatched) {
            isWatched = false
            removeOnAttachStateChangeListener(this@ShadowSwitch)
            if (isAttachedToWindow) onDetached(view)
            shadow?.detachFromTarget()
        }
    }

    private fun onAttached(view: View) {
        val shadow = view.shadow
        if (shadow == null) {
            view.createShadow()
        } else if (view.isRecyclingViewGroupChild) {
            shadow.show()
        }
    }

    private fun onDetached(view: View) {
        val shadow = view.shadow ?: return
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

    private var View.isWatched: Boolean
        get() = getTag(R.id.is_watched) == true
        set(value) = setTag(R.id.is_watched, value)
}

private fun View.createShadow() {
    val parent = parent as? ViewGroup ?: return
    if (clippedShadowPlane == ClippedShadowPlane.Inline) {
        if (parent is ClippedShadowsViewGroup && !parent.ignoreInlineChildShadows) {
            parent.inlineController.createGroupShadow(this)
        } else {
            IndependentShadow(this)
        }
    } else {
        parent.getOrCreateOverlayController().createGroupShadow(this)
    }
}

internal fun View.recreateShadow() {
    shadow?.detachFromTarget() ?: return
    createShadow()
}