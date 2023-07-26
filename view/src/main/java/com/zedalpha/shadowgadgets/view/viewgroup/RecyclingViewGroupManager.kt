package com.zedalpha.shadowgadgets.view.viewgroup

import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.clippedShadowPlane
import com.zedalpha.shadowgadgets.view.shadow.overlayController


internal class RecyclingViewGroupManager(
    parentView: ViewGroup,
    attributeSet: AttributeSet?,
    detachAllViewsFromParent: () -> Unit,
    attachViewToParent: (View, Int, ViewGroup.LayoutParams) -> Unit
) : ClippedShadowsViewGroupManager(
    parentView,
    attributeSet,
    detachAllViewsFromParent,
    attachViewToParent
) {
    override fun onParentViewDetached() {
        parentView.overlayController?.detachAllShadows()
        detachAllShadows()
    }

    override fun onViewAdded(child: View) {
        if (!child.isRecyclingViewGroupChild) {
            child.isRecyclingViewGroupChild = true
            childClippedShadowsPlane?.let { child.clippedShadowPlane = it }
            child.clipOutlineShadow = true
        }
    }
}

internal var View.isRecyclingViewGroupChild: Boolean
    get() = getTag(R.id.recycling_view_group_child) == true
    private set(value) = setTag(R.id.recycling_view_group_child, value)