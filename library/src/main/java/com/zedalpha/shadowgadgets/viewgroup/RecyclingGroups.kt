@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.ExpandableListView
import android.widget.GridView
import android.widget.ListView
import android.widget.StackView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView


class ClippedShadowsRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs), ClippedShadowsViewGroup {
    private val manager = RecyclingShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}


class ClippedShadowsExpandableListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.expandableListViewStyle,
    defStyleRes: Int = 0
) : ExpandableListView(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = RecyclingShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}


class ClippedShadowsGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.gridViewStyle,
    defStyleRes: Int = 0
) : GridView(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = RecyclingShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}


class ClippedShadowsListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.listViewStyle,
    defStyleRes: Int = 0
) : ListView(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = RecyclingShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}


class ClippedShadowStackView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.stackViewStyle,
    defStyleRes: Int = 0
) : StackView(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = RecyclingShadowManager(this, attrs)

    override val isUsingShadowsFallback by manager::isUsingShadowsFallback

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childClippedShadowsPlane by manager::childClippedShadowsPlane

    override var childShadowsFallbackStrategy by manager::childShadowsFallbackStrategy

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }
}