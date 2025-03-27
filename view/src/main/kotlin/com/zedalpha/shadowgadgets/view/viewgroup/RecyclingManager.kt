package com.zedalpha.shadowgadgets.view.viewgroup

import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadow.createShadow
import com.zedalpha.shadowgadgets.view.shadow.isInitializedForRecyclingSVG
import com.zedalpha.shadowgadgets.view.shadowPlane

internal class RecyclingManager(
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
    override fun onViewAdded(child: View) {
        if (child.isInitializedForRecyclingSVG) return

        if (child.planeNotSet && groupPlaneSet) {
            child.shadowPlane = childShadowsPlane
        }

        // Recycling SVGs clip all of their children's shadows by default.
        if (child.clipNotSet) child.clipOutlineShadow =
            if (groupClipSet) clipAllChildShadows else true

        if (child.colorNotSet && groupColorSet) {
            child.outlineShadowColorCompat =
                childOutlineShadowsColorCompat
        }

        if (child.forceNotSet && groupForceSet) {
            child.forceOutlineShadowColorCompat =
                forceChildOutlineShadowsColorCompat
        }

        child.isInitializedForRecyclingSVG = true

        child.createShadow()
    }
}