package com.zedalpha.shadowgadgets.view.internal

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.plane.updatePlane
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy
import com.zedalpha.shadowgadgets.view.tintOutlineShadow
import com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup
import com.zedalpha.shadowgadgets.view.viewgroup.isInitializedForRecyclingSVG

internal fun updateShadow(target: View) {
    val requiresProxy = target.clipOutlineShadow || target.tintOutlineShadow
    val isSwitched = target.isSwitched
    val proxy = target.shadowProxy

    when {
        requiresProxy && !isSwitched -> {
            target.isSwitched = true
            target.addOnAttachStateChangeListener(ShadowSwitch)
            if (target.isAttachedToWindow) handleAttach(target)
        }
        !requiresProxy && isSwitched -> {
            target.isSwitched = false
            target.removeOnAttachStateChangeListener(ShadowSwitch)
            proxy?.dispose()
        }
    }
}

private var View.isSwitched: Boolean by viewTag(R.id.is_switched, false)

private object ShadowSwitch : View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(view: View) = handleAttach(view)
    override fun onViewDetachedFromWindow(view: View) = handleDetach(view)
}

private fun handleAttach(target: View) {
    val proxy = target.shadowProxy
    val parent = target.parentViewGroup

    if (proxy == null || proxy.plane?.viewGroup !== parent) {
        val isParentRecyclingViewGroup = parent?.isRecyclingViewGroup == true
        target.isRecyclingViewGroupChild = isParentRecyclingViewGroup

        // Recycling SVGs create proxies for their children upon first add.
        if (!(isParentRecyclingViewGroup && parent is ShadowsViewGroup) ||
            target.isInitializedForRecyclingSVG  // Reparented from a RecSVG.
        ) {
            createProxy(target)
        }
    } else {
        proxy.isShown = true
    }
}

private fun handleDetach(target: View) {
    val proxy = target.shadowProxy ?: return

    if (target.isRecyclingViewGroupChild) {
        proxy.isShown = false
    } else {
        proxy.dispose()
    }
}

internal fun createProxy(target: View) =
    ShadowProxy(target).also { updatePlane(it) }

internal val ViewGroup.isRecyclingViewGroup: Boolean
    get() = this is RecyclerView || this is AdapterView<*>

internal var View.isRecyclingViewGroupChild: Boolean
        by viewTag(R.id.is_recycling_view_group_child, false)