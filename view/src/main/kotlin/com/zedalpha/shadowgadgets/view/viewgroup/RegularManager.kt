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

internal class RegularManager(
    parentView: ViewGroup,
    attributeSet: AttributeSet?,
    attachViewToParent: (View, Int, ViewGroup.LayoutParams) -> Unit,
    detachAllViewsFromParent: () -> Unit,
    superDispatchDraw: (Canvas) -> Unit,
    superDrawChild: (Canvas, View, Long) -> Boolean
) : ShadowsViewGroupManager(
    parentView,
    attributeSet,
    attachViewToParent,
    detachAllViewsFromParent,
    superDispatchDraw,
    superDrawChild
) {
    private var xmlAttributes: MutableMap<Int, ShadowAttributes>? =
        mutableMapOf()

    fun generateLayoutParams(attributeSet: AttributeSet?) {
        if (attached) return
        val attributes =
            attributeSet.extractShadowAttributes(parentView.context)
        if (attributes.id != View.NO_ID) {
            xmlAttributes?.put(attributes.id, attributes)
        }
    }

    override fun onParentViewAttached() {
        super.onParentViewAttached()
        xmlAttributes = null
    }

    override fun onViewAdded(child: View) {
        if (attached) return

        val attributes = xmlAttributes?.remove(child.id)
        if (attributes == null) {
            if (child.planeNotSet && groupPlaneSet) {
                child.shadowPlane = childShadowsPlane
            }

            if (child.clipNotSet && groupClipSet) {
                child.clipOutlineShadow = clipAllChildShadows
            }

            if (child.colorNotSet && groupColorSet) {
                child.outlineShadowColorCompat =
                    childOutlineShadowsColorCompat
            }

            if (child.forceNotSet && groupForceSet) {
                child.forceOutlineShadowColorCompat =
                    forceChildOutlineShadowsColorCompat
            }
        } else {
            with(attributes) {
                val plane = shadowPlane
                    ?: childShadowsPlane.takeIf { groupPlaneSet }
                plane?.let { child.shadowPlane = it }

                val clip = clipOutlineShadow
                    ?: clipAllChildShadows.takeIf { groupClipSet }
                clip?.let { child.clipOutlineShadow = it }

                val color = outlineShadowColorCompat
                    ?: childOutlineShadowsColorCompat.takeIf { groupColorSet }
                color?.let { child.outlineShadowColorCompat = it }

                val force = forceOutlineShadowColorCompat
                    ?: forceChildOutlineShadowsColorCompat.takeIf { groupForceSet }
                force?.let { child.forceOutlineShadowColorCompat = it }
            }
        }
    }
}