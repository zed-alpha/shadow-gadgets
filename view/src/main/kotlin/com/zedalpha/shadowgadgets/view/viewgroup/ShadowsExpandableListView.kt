package com.zedalpha.shadowgadgets.view.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.ExpandableListView
import com.zedalpha.shadowgadgets.view.ShadowPlane

/**
 * A custom [ExpandableListView] that implements [ShadowsViewGroup].
 *
 * Apart from the additional handling of the library's shadow properties and
 * draw operations, this group behaves just like its base class.
 */
public class ShadowsExpandableListView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.expandableListViewStyle,
    defStyleRes: Int = 0
) : ExpandableListView(context, attrs, defStyleAttr, defStyleRes),
    ShadowsViewGroup {

    internal val manager =
        RecyclingManager(
            viewGroup = this,
            attributeSet = attrs,
            attachViewToParent = this::attachViewToParent,
            detachAllViewsFromParent = this::detachAllViewsFromParent,
            superDispatchDraw = { c -> super.dispatchDraw(c) },
            superDrawChild = { c, v, t -> super.drawChild(c, v, t) }
        )

    override var childShadowsPlane: ShadowPlane
            by manager::childShadowsPlane

    override var clipAllChildShadows: Boolean
            by manager::clipAllChildShadows

    override var childOutlineShadowsColorCompat: Int
            by manager::childOutlineShadowsColorCompat

    override var forceChildOutlineShadowsColorCompat: Boolean
            by manager::forceChildOutlineShadowsColorCompat

    override var ignoreInlineChildShadows: Boolean
            by manager::ignoreInlineChildShadows

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }

    override fun dispatchDraw(canvas: Canvas): Unit =
        manager.dispatchDraw(canvas)

    override fun drawChild(
        canvas: Canvas,
        child: View,
        drawingTime: Long
    ): Boolean =
        manager.drawChild(canvas, child, drawingTime)
}