@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ListView
import androidx.annotation.RequiresApi

class ClippedShadowsListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.listViewStyle,
    defStyleRes: Int = 0
) : ListView(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = ViewGroupShadowManager(this, attrs)

    override val isUsingShadowsFallback = manager.isUsingFallback

    override var clipAllChildShadows by manager::clipAllChildShadows
    override var childClippedShadowsPlane by manager::childClippedShadowsPlane
    override var disableChildShadowsOnFallback by manager::disableChildShadowsOnFallback

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child, true)
    }
}