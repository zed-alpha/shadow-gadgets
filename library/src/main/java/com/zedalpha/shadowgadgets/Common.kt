@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.overlay.OverlayShadowSwitch
import com.zedalpha.shadowgadgets.overlay.moveShadowToOverlay
import com.zedalpha.shadowgadgets.overlay.removeShadowFromOverlay
import com.zedalpha.shadowgadgets.shadow.shadow
import com.zedalpha.shadowgadgets.viewgroup.ViewGroupShadow

var View.clipOutlineShadow: Boolean
    get() = getTag(R.id.tag_target_overlay_shadow_switch) is OverlayShadowSwitch
    set(value) {
        if (value == clipOutlineShadow || shadow is ViewGroupShadow) return
        if (value) {
            setTag(R.id.tag_target_overlay_shadow_switch, OverlayShadowSwitch)
            addOnAttachStateChangeListener(OverlayShadowSwitch)
            if (isAttachedToWindow) moveShadowToOverlay(this)
        } else {
            setTag(R.id.tag_target_overlay_shadow_switch, null)
            removeOnAttachStateChangeListener(OverlayShadowSwitch)
            if (isAttachedToWindow) removeShadowFromOverlay(this)
        }
    }