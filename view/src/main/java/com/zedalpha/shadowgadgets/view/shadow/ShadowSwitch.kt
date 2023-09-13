package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.shadowPlane
import com.zedalpha.shadowgadgets.view.viewgroup.RecyclingManager
import com.zedalpha.shadowgadgets.view.viewgroup.inlineController


internal fun View.notifyPropertyChanged() {
    if (clipOutlineShadow || colorOutlineShadow) {
        if (isWatched) {
            if (shadow?.checkRecreate() == true) recreateShadow()
        } else {
            isWatched = true
            addOnAttachStateChangeListener(ShadowSwitch)
            if (isAttachedToWindow) notifyAttached()
        }
    } else if (isWatched) {
        isWatched = false
        removeOnAttachStateChangeListener(ShadowSwitch)
        shadow?.detachFromTarget()
    }
}

private object ShadowSwitch : View.OnAttachStateChangeListener {

    override fun onViewAttachedToWindow(view: View) {
        view.notifyAttached()
    }

    override fun onViewDetachedFromWindow(view: View) {
        view.notifyDetached()
    }
}

private fun View.notifyAttached() {
    checkRecyclingManager()
    val shadow = shadow
    if (shadow == null) {
        createShadow()
    } else {
        shadow.isShown = true
    }
}

private fun View.notifyDetached() {
    val shadow = shadow ?: return
    if (recyclingManager == null) {
        shadow.detachFromTarget()
    } else {
        shadow.isShown = false
    }
}

private fun View.createShadow() {
    val parent = parent as? ViewGroup ?: return
    if (shadowPlane != ShadowPlane.Inline) {
        parent.getOrCreateOverlayController().createShadowForView(this)
    } else {
        @Suppress("DEPRECATION")
        if (parent is com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsViewGroup &&
            !parent.ignoreInlineChildShadows
        ) {
            parent.inlineController.createShadowForView(this)
        } else {
            SoloShadow(this)
        }
    }
}

internal fun View.recreateShadow() {
    shadow?.detachFromTarget() ?: return
    createShadow()
}

private fun View.checkRecyclingManager() {
    val current = recyclingManager
    val next = (parent as? ViewGroup)?.let { parent ->
        @Suppress("DEPRECATION")
        if (parent is com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsViewGroup) {
            parent.inlineController as? RecyclingManager
        } else {
            null
        }
    }
    if (current != null && current != next) shadow?.detachFromTarget()
    recyclingManager = next
}

private var View.isWatched: Boolean
    get() = getTag(R.id.is_watched) == true
    set(value) = setTag(R.id.is_watched, value)

private var View.recyclingManager: RecyclingManager?
    get() = getTag(R.id.recycling_manager) as? RecyclingManager
    set(value) = setTag(R.id.recycling_manager, value)