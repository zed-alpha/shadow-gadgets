package com.zedalpha.shadowgadgets.view.viewgroup

import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.clippedShadowPlane
import com.zedalpha.shadowgadgets.view.forceShadowColorCompat
import com.zedalpha.shadowgadgets.view.internal.ClippedShadowAttributes
import com.zedalpha.shadowgadgets.view.internal.extractShadowAttributes
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat

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
            if (attributes != null) {
                child.outlineShadowColorCompat =
                    attributes.outlineShadowColorCompat
                child.forceShadowColorCompat =
                    attributes.forceShadowColorCompat
                child.clippedShadowPlane =
                    attributes.clippedShadowPlane
                child.clipOutlineShadow =
                    attributes.clipOutlineShadow
            } else {
                if (planeSet) child.clippedShadowPlane =
                    childClippedShadowsPlane
                if (clipSet) child.clipOutlineShadow =
                    clipAllChildShadows
            }
        }
    }
}