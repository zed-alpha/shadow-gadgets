package com.zedalpha.shadowgadgets.view.viewgroup

import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane

internal class RecyclingManager(
    parentView: ViewGroup,
    attributeSet: AttributeSet?,
    detachAllViewsFromParent: () -> Unit,
    attachViewToParent: (View, Int, ViewGroup.LayoutParams) -> Unit
) : ShadowsViewGroupManager(
    parentView,
    attributeSet,
    detachAllViewsFromParent,
    attachViewToParent
) {
    override fun onViewAdded(child: View) {
        if (child.isInitialized) return

        if (child.planeNotSet && groupPlaneSet) {
            child.shadowPlane = childShadowsPlane
        }

        // Recycling SVGs clip all of their children's shadows by default
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

        child.isInitialized = true
    }

    private var View.isInitialized: Boolean
        get() = getTag(R.id.is_initialized) == true
        set(value) = setTag(R.id.is_initialized, value)
}