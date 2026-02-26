package com.zedalpha.shadowgadgets.view.proxy

import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.internal.parentViewGroup
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.tintOutlineShadow
import com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup
import com.zedalpha.shadowgadgets.view.viewgroup.isInitializedForRecyclingSVG

internal fun View.updateProxy() {
    val requiresProxy = this.clipOutlineShadow || this.tintOutlineShadow
    val isSwitched = this.isSwitched

    when {
        requiresProxy && !isSwitched -> {
            this.isSwitched = true
            this.addOnAttachStateChangeListener(ShadowSwitch)
            if (this.isAttachedToWindow) handleAttach(this)
        }
        !requiresProxy && isSwitched -> {
            this.isSwitched = false
            this.removeOnAttachStateChangeListener(ShadowSwitch)
            this.shadowProxy?.dispose()
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

    if (proxy != null) {
        check(proxy.isChildOfRecyclingViewGroup) {
            "Non-Recycling ViewGroup child attached with Proxy"
        }
        if (proxy.plane.viewGroup === parent) {
            proxy.isShown = true
        } else {
            proxy.updatePlane()
        }
    } else {
        if (parent is ShadowsViewGroup && parent.isRecycling &&
            !target.isInitializedForRecyclingSVG
        ) {
            return
        }
        createProxy(target)
    }
}

private fun handleDetach(target: View) {
    val proxy = checkNotNull(target.shadowProxy) { "Null Proxy on detach" }

    if (proxy.isChildOfRecyclingViewGroup) {
        proxy.isShown = false
    } else {
        proxy.dispose()
    }
}

internal fun createProxy(target: View) =
    ShadowProxy(target).apply { updatePlane() }

internal val ViewGroup.isRecycling: Boolean
    get() = this is RecyclerView || this is AdapterView<*>