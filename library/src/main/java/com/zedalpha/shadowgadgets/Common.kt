@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets

import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.ClippedShadowPlane.Foreground
import com.zedalpha.shadowgadgets.ShadowFallbackStrategy.None
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

var View.shadowFallbackStrategy: ShadowFallbackStrategy
    get() = getTag(R.id.tag_target_shadow_fallback_strategy) as? ShadowFallbackStrategy ?: None
    set(value) {
        if (value == shadowFallbackStrategy) return
        setTag(R.id.tag_target_shadow_fallback_strategy, value)
        (shadow as? ViewShadow)?.notifyAttributeChanged()
    }

enum class ClippedShadowPlane {
    Foreground, Background;

    internal companion object {
        fun forValue(value: Int) = if (value == 1) Background else Foreground
    }
}

enum class ShadowFallbackStrategy {
    None, ForegroundOnly, Disable;

    internal companion object {
        fun forValue(value: Int) = when (value) {
            1 -> ForegroundOnly
            2 -> Disable
            else -> None
        }
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
        val shadow = view.shadow
        if (view.isRecyclingViewGroupChild && shadow != null) {
            shadow.show()
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