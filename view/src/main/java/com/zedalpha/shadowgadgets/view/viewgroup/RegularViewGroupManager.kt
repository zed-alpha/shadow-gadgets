package com.zedalpha.shadowgadgets.view.viewgroup

import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.clippedShadowPlane
import com.zedalpha.shadowgadgets.view.internal.ClippedShadowAttributes
import com.zedalpha.shadowgadgets.view.internal.extractShadowAttributes

internal class RegularViewGroupManager(
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
    private var xmlAttributes: MutableMap<Int, ClippedShadowAttributes>? =
        mutableMapOf()

    fun generateLayoutParams(attributeSet: AttributeSet?) {
        if (!unattached) return
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
        if (unattached) {
            val attributes = xmlAttributes?.remove(child.id)

            val plane =
                attributes?.clippedShadowPlane ?: childClippedShadowsPlane
            plane?.let { child.clippedShadowPlane = it }

            val clip = attributes?.clipOutlineShadow ?: clipAllChildShadows
            clip?.let { child.clipOutlineShadow = it }
        }
    }
}