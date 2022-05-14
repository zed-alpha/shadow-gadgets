@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.google.android.material.chip.ChipGroup
import com.zedalpha.shadowgadgets.R

class ClippedShadowsChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.chipGroupStyle
) : ChipGroup(context, attrs, defStyleAttr), ClippedShadowsViewGroup {
    private val manager = ViewGroupShadowManager(this, attrs)

    override val isUsingShadowsFallback = manager.isUsingFallback

    override val clipAllChildShadows by manager::clipAllChildShadows
    override val childClippedShadowsPlane by manager::childClippedShadowsPlane
    override val childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        manager.generateLayoutParams(attrs)
        return super.generateLayoutParams(attrs)
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}