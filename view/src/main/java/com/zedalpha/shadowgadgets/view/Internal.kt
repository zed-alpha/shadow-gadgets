package com.zedalpha.shadowgadgets.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.shadow.OverlayShadow
import com.zedalpha.shadowgadgets.view.shadow.getOrCreateController
import com.zedalpha.shadowgadgets.view.shadow.shadow
import com.zedalpha.shadowgadgets.view.viewgroup.isRecyclingViewGroupChild


internal object ShadowSwitch : View.OnAttachStateChangeListener {

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
        val shadow = view.shadow
        if (shadow is OverlayShadow && view.isRecyclingViewGroupChild) {
            shadow.show()
        } else if (view.outlineProvider != null) {
            createShadowForView(view)
        }
    }

    private fun onDetached(view: View) {
        val shadow = view.shadow ?: return
        if (shadow is OverlayShadow && view.isRecyclingViewGroupChild) {
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

internal fun createShadowForView(view: View) {
    val parent = view.parent as? ViewGroup ?: return
    getOrCreateController(parent).addOverlayShadow(view)
}

internal fun View.recreateShadow() {
    val oldShadow = shadow ?: return
    oldShadow.notifyDetach()
    createShadowForView(this)
    invalidate()
}

internal data class ClippedShadowAttributes(
    val id: Int,
    val clipOutlineShadow: Boolean? = null,
    val clippedShadowPlane: ClippedShadowPlane? = null
)

internal fun AttributeSet?.extractShadowAttributes(
    context: Context
): ClippedShadowAttributes {
    val array = context.obtainStyledAttributes(
        this,
        R.styleable.ClippedShadowAttributes
    )
    return ClippedShadowAttributes(
        array.getResourceId(
            R.styleable.ClippedShadowAttributes_android_id,
            View.NO_ID
        ),
        if (array.hasValue(R.styleable.ClippedShadowAttributes_clipOutlineShadow)) {
            array.getBoolean(
                R.styleable.ClippedShadowAttributes_clipOutlineShadow,
                false
            )
        } else null,
        if (array.hasValue(R.styleable.ClippedShadowAttributes_clippedShadowPlane)) {
            ClippedShadowPlane.forValue(
                array.getInt(
                    R.styleable.ClippedShadowAttributes_clippedShadowPlane,
                    0
                )
            )
        } else null
    ).also { array.recycle() }
}