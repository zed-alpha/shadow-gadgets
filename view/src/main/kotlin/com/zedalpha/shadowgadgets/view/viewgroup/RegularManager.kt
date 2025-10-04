package com.zedalpha.shadowgadgets.view.viewgroup

import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.internal.ShadowAttributes
import com.zedalpha.shadowgadgets.view.internal.extractShadowAttributes
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane

internal class RegularManager<T>(
    viewGroup: T,
    attributeSet: AttributeSet?,
    attachViewToParent: (View, Int, ViewGroup.LayoutParams) -> Unit,
    detachAllViewsFromParent: () -> Unit,
    superDispatchDraw: (Canvas) -> Unit,
    superDrawChild: (Canvas, View, Long) -> Boolean
) : ShadowsViewGroupManager<T>(
    viewGroup,
    attributeSet,
    attachViewToParent,
    detachAllViewsFromParent,
    superDispatchDraw,
    superDrawChild
) where T : ViewGroup, T : ShadowsViewGroup {

    private var xmlAttributes: MutableMap<Int, ShadowAttributes>? =
        mutableMapOf()

    override fun onAttach() {
        super.onAttach()
        xmlAttributes = null
    }

    fun generateLayoutParams(attributeSet: AttributeSet?) {
        if (isAttached) return

        val context = viewGroup.context
        val attributes = attributeSet.extractShadowAttributes(context)
        if (attributes.id == View.NO_ID) return

        xmlAttributes?.put(attributes.id, attributes)
    }

    override fun onViewAdded(child: View) {
        if (isAttached) return

        val attributes = xmlAttributes?.remove(child.id)
        if (attributes == null) {
            if (child.isPlaneNotSet && isGroupPlaneSet) {
                child.shadowPlane = childShadowsPlane
            }

            if (child.isClipNotSet && isGroupClipSet) {
                child.clipOutlineShadow = clipAllChildShadows
            }

            if (child.isColorNotSet && isGroupColorSet) {
                child.outlineShadowColorCompat =
                    childOutlineShadowsColorCompat
            }

            if (child.isForceNotSet && isGroupForceSet) {
                child.forceOutlineShadowColorCompat =
                    forceChildOutlineShadowsColorCompat
            }
        } else {
            val plane = attributes.shadowPlane
                ?: childShadowsPlane.takeIf { isGroupPlaneSet }
            plane?.let { child.shadowPlane = it }

            val clip = attributes.clipOutlineShadow
                ?: clipAllChildShadows.takeIf { isGroupClipSet }
            clip?.let { child.clipOutlineShadow = it }

            val color = attributes.outlineShadowColorCompat
                ?: childOutlineShadowsColorCompat.takeIf { isGroupColorSet }
            color?.let { child.outlineShadowColorCompat = it }

            val force = attributes.forceOutlineShadowColorCompat
                ?: forceChildOutlineShadowsColorCompat.takeIf { isGroupForceSet }
            force?.let { child.forceOutlineShadowColorCompat = it }
        }
    }
}