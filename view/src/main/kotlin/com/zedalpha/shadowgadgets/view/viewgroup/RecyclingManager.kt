package com.zedalpha.shadowgadgets.view.viewgroup

import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.internal.createProxy
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.shadowPlane
import com.zedalpha.shadowgadgets.view.tintOutlineShadow

internal class RecyclingManager<T>(
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

    override fun onViewAdded(child: View) {
        if (child.isInitializedForRecyclingSVG) return

        if (child.isPlaneNotSet && isGroupPlaneSet) {
            child.shadowPlane = childShadowsPlane
        }

        // Recycling SVGs clip all of their children's shadows by default.
        if (child.isClipNotSet) {
            child.clipOutlineShadow =
                if (isGroupClipSet) clipAllChildShadows else true
        }

        if (child.isColorNotSet && isGroupColorSet) {
            child.outlineShadowColorCompat =
                childOutlineShadowsColorCompat
        }

        if (child.isForceNotSet && isGroupForceSet) {
            child.forceOutlineShadowColorCompat =
                forceChildOutlineShadowsColorCompat
        }

        child.isInitializedForRecyclingSVG = true

        if (child.clipOutlineShadow || child.tintOutlineShadow) {
            createProxy(child)
        }
    }
}

internal var View.isInitializedForRecyclingSVG: Boolean
        by viewTag(R.id.is_initialized_for_recycling_svg, false)
    private set