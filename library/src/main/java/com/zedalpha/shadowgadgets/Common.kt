package com.zedalpha.shadowgadgets

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import com.zedalpha.shadowgadgets.overlay.ShadowSwitch
import com.zedalpha.shadowgadgets.overlay.moveShadowToOverlay
import com.zedalpha.shadowgadgets.overlay.removeShadowFromOverlay


var View.clipOutlineShadow: Boolean
    get() = getTag(R.id.tag_target_overlay_shadow_switch) is ShadowSwitch
    set(value) {
        if (value) {
            if (!clipOutlineShadow) {
                setTag(R.id.tag_target_overlay_shadow_switch, ShadowSwitch)
                addOnAttachStateChangeListener(ShadowSwitch)
                if (isAttachedToWindow) moveShadowToOverlay(this)
            }
        } else {
            if (clipOutlineShadow) {
                setTag(R.id.tag_target_overlay_shadow_switch, null)
                removeOnAttachStateChangeListener(ShadowSwitch)
                if (isAttachedToWindow) removeShadowFromOverlay(this)
            }
        }
    }

internal class ShadowXmlAttributes(val clip: Boolean, val disableAnimation: Boolean)

internal var View.shadowXmlAttributes: ShadowXmlAttributes?
    get() = getTag(R.id.tag_target_shadow_xml_attributes) as? ShadowXmlAttributes
    set(value) {
        setTag(R.id.tag_target_shadow_xml_attributes, value)
    }

internal fun unwrapActivity(context: Context): Activity? {
    var checkContext: Context? = context
    do {
        if (checkContext is Activity) return checkContext
        checkContext = (checkContext as? ContextWrapper)?.baseContext
    } while (checkContext != null)
    return null
}