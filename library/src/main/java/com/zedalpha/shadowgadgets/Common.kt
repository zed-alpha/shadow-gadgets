@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.shadow.ShadowSwitch
import com.zedalpha.shadowgadgets.shadow.moveShadowToOverlay
import com.zedalpha.shadowgadgets.shadow.removeShadowFromOverlay


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


private fun Context.getAttributeBoolean(
    attrs: AttributeSet?,
    attrArray: IntArray
): Boolean {
    val array = obtainStyledAttributes(attrs, attrArray)
    val clip = array.getBoolean(0, false)
    array.recycle()
    return clip
}

private val ClipOutlineShadowAttribute = intArrayOf(R.attr.clipOutlineShadow)
private val ClipAllChildShadowsAttribute = intArrayOf(R.attr.clipAllChildShadows)
private val DisableShadowsOnFallbackAttribute = intArrayOf(R.attr.disableShadowsOnFallback)

internal fun Context.getClipOutlineShadow(attrs: AttributeSet?) =
    getAttributeBoolean(attrs, ClipOutlineShadowAttribute)

internal fun Context.getClipAllChildShadows(attrs: AttributeSet?) =
    getAttributeBoolean(attrs, ClipAllChildShadowsAttribute)

internal fun Context.getDisableShadowsOnFallback(attrs: AttributeSet?) =
    getAttributeBoolean(attrs, DisableShadowsOnFallbackAttribute)