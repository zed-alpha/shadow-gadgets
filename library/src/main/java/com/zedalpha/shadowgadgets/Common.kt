@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.overlay.ShadowSwitch
import com.zedalpha.shadowgadgets.overlay.moveShadowToOverlay
import com.zedalpha.shadowgadgets.overlay.removeShadowFromOverlay


var View.clipOutlineShadow: Boolean
    get() = getTag(R.id.tag_target_overlay_shadow_switch) is ShadowSwitch
    set(value) {
        if (value != clipOutlineShadow) {
            if (value) {
                setTag(R.id.tag_target_overlay_shadow_switch, ShadowSwitch)
                addOnAttachStateChangeListener(ShadowSwitch)
                if (isAttachedToWindow) moveShadowToOverlay(this)
            } else {
                setTag(R.id.tag_target_overlay_shadow_switch, null)
                removeOnAttachStateChangeListener(ShadowSwitch)
                if (isAttachedToWindow) removeShadowFromOverlay(this)
            }
        }
    }

internal fun unwrapActivity(context: Context): Activity? {
    var checkContext: Context? = context
    do {
        if (checkContext is Activity) return checkContext
        checkContext = (checkContext as? ContextWrapper)?.baseContext
    } while (checkContext != null)
    return null
}