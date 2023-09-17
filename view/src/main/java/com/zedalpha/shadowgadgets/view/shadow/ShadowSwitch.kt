package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.shadowPlane
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
    val shadow = shadow
    val parentId = (parent as? View)?.let { parent ->
        "${parent.javaClass.name}:${parent.hashCode()}"
    }
    if (lastParentId != parentId) {
        shadow?.detachFromTarget()
        lastParentId = parentId
    }
    if (shadow != null) {
        shadow.isShown = true
    } else {
        createShadow()
    }
}

private fun View.notifyDetached() {
    val shadow = shadow ?: return
    if (isRecyclingViewGroupChild) {
        shadow.isShown = false
    } else {
        shadow.detachFromTarget()
    }
}

private fun View.createShadow() {
    val parent = parent as? ViewGroup ?: return
    isRecyclingViewGroupChild = RecyclingViewGroupClasses.any { groupClass ->
        groupClass.isAssignableFrom(parent.javaClass)
    }
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

private var View.isWatched: Boolean
    get() = getTag(R.id.is_watched) == true
    set(value) = setTag(R.id.is_watched, value)

private var View.lastParentId: String?
    get() = getTag(R.id.last_parent_id) as? String
    set(value) = setTag(R.id.last_parent_id, value)

private var View.isRecyclingViewGroupChild: Boolean
    get() = getTag(R.id.is_recycling_view_group_child) == true
    set(value) = setTag(R.id.is_recycling_view_group_child, value)