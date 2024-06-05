package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.shadowPlane
import com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup
import com.zedalpha.shadowgadgets.view.viewgroup.inlineController
import java.lang.ref.WeakReference

internal object ShadowSwitch : View.OnAttachStateChangeListener {

    fun notifyPropertyChanged(view: View) {
        if (view.clipOutlineShadow || view.colorOutlineShadow) {
            if (view.isWatched) {
                view.shadow?.let { shadow ->
                    if (view.clipOutlineShadow != shadow.isClipped) {
                        recreateShadow(view)
                    }
                }
            } else {
                view.isWatched = true
                view.addOnAttachStateChangeListener(this)
                if (view.isAttachedToWindow) handleAttach(view)
            }
        } else if (view.isWatched) {
            view.isWatched = false
            view.removeOnAttachStateChangeListener(this)
            view.shadow?.detachFromTarget()
        }
    }

    fun recreateShadow(view: View) {
        view.shadow?.detachFromTarget() ?: return
        createShadow(view)
    }

    override fun onViewAttachedToWindow(view: View) {
        handleAttach(view)
    }

    override fun onViewDetachedFromWindow(view: View) {
        handleDetach(view)
    }
}

private fun handleAttach(view: View) {
    val parent = view.parent as? ViewGroup
    if (view.isRecyclingViewGroupChild && view.previousParent != parent) {
        view.shadow?.detachFromTarget()
        view.previousParent = parent
    }
    val shadow = view.shadow
    if (shadow != null) {
        shadow.isShown = true
    } else {
        createShadow(view)
    }
}

private fun handleDetach(view: View) {
    val shadow = view.shadow ?: return
    if (view.isRecyclingViewGroupChild) {
        shadow.isShown = false
    } else {
        shadow.detachFromTarget()
    }
}

private fun createShadow(view: View) {
    val parent = view.parent as? ViewGroup ?: return
    view.isRecyclingViewGroupChild = parent.isRecyclingViewGroup
    when {
        view.shadowPlane != ShadowPlane.Inline -> {
            parent.getOrCreateOverlayController().createShadow(view)
        }

        parent is ShadowsViewGroup && !parent.ignoreInlineChildShadows -> {
            parent.inlineController.createShadow(view)
        }

        else -> SoloShadow(view)
    }
}

private inline var View.isWatched: Boolean
    get() = getTag(R.id.is_watched) == true
    set(value) = setTag(R.id.is_watched, value)

private inline var View.isRecyclingViewGroupChild: Boolean
    get() = getTag(R.id.is_recycling_view_group_child) == true
    set(value) = setTag(R.id.is_recycling_view_group_child, value)

@get:Suppress("UNCHECKED_CAST")
private inline var View.previousParent: ViewGroup?
    get() = (getTag(R.id.previous_parent) as? WeakReference<ViewGroup>)?.get()
    set(value) = setTag(R.id.previous_parent, value?.let { WeakReference(it) })