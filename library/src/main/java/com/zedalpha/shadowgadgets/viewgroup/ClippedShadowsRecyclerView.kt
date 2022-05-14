@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView

class ClippedShadowsRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs), ClippedShadowsViewGroup {
    private val manager = ViewGroupShadowManager(this, attrs, true)

    override val isUsingShadowsFallback = manager.isUsingFallback

    override val clipAllChildShadows by manager::clipAllChildShadows
    override val childClippedShadowsPlane by manager::childClippedShadowsPlane
    override val childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}