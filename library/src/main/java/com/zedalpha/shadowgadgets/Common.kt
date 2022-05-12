@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.ClippedShadowPlane.Foreground
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.shadow.ViewShadow
import com.zedalpha.shadowgadgets.shadow.getOrCreateController
import com.zedalpha.shadowgadgets.shadow.shadow


var View.clipOutlineShadow: Boolean
    get() = getTag(R.id.tag_target_clip_outline_shadow) as? Boolean ?: false
    set(value) {
        if (value == clipOutlineShadow) return
        setTag(R.id.tag_target_clip_outline_shadow, value)
        ShadowSwitch.notifyClipChanged(this, value)
    }

var View.clippedShadowPlane: ClippedShadowPlane
    get() = getTag(R.id.tag_target_clipped_shadow_plane) as? ClippedShadowPlane ?: Foreground
    set(value) {
        if (value == clippedShadowPlane) return
        setTag(R.id.tag_target_clipped_shadow_plane, value)
        shadow?.notifyAttributeChanged()
    }

var View.disableShadowOnFallback: Boolean
    get() = getTag(R.id.tag_target_disable_shadow_on_fallback) as? Boolean ?: false
    set(value) {
        if (value == disableShadowOnFallback) return
        setTag(R.id.tag_target_disable_shadow_on_fallback, value)
        (shadow as? ViewShadow)?.notifyAttributeChanged()
    }

enum class ClippedShadowPlane {
    Foreground, Background;

    internal companion object {
        fun forValue(value: Int) = if (value == 1) Background else Foreground
    }
}

object ShadowGadgets {
    val isUsingShadowsFallback = !RenderNodeFactory.isOpenForBusiness
}

private object ShadowSwitch : View.OnAttachStateChangeListener {
    fun notifyClipChanged(view: View, turnOn: Boolean) {
        if (turnOn) {
            view.addOnAttachStateChangeListener(this)
            if (view.isAttachedToWindow) onAttached(view)
        } else {
            view.removeOnAttachStateChangeListener(this)
            if (view.isAttachedToWindow) onDetached(view)
            view.shadow?.notifyDetach()
        }
    }

    private fun onAttached(view: View) {
        val parent = view.parent as? ViewGroup ?: return
        if (view.isRecyclingViewGroupChild) {
            val shadow = view.shadow
            if (shadow == null) {
                getOrCreateController(parent).addShadowForView(view)
            } else {
                shadow.show()
            }
        } else {
            getOrCreateController(parent).addShadowForView(view)
        }
    }

    private fun onDetached(view: View) {
        val shadow = view.shadow ?: return
        if (view.isRecyclingViewGroupChild) {
            shadow.hide()
        } else {
            shadow.notifyDetach()
        }
    }

    override fun onViewAttachedToWindow(view: View) {
        onAttached(view)
    }

    override fun onViewDetachedFromWindow(view: View) {
        onDetached(view)
    }
}

internal var View.isRecyclingViewGroupChild: Boolean
    get() = getTag(R.id.tag_target_recycling_view_group_child) as? Boolean ?: false
    set(value) {
        setTag(R.id.tag_target_recycling_view_group_child, value)
    }

internal val View.isClippedShadowPlaneExplicitlySet: Boolean
    get() = getTag(R.id.tag_target_clipped_shadow_plane) is ClippedShadowPlane

internal val View.isDisableShadowOnFallbackExplicitlySet: Boolean
    get() = getTag(R.id.tag_target_disable_shadow_on_fallback) is Boolean