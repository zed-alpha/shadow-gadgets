package com.zedalpha.shadowgadgets.view.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.widget.ListView
import com.zedalpha.shadowgadgets.view.ShadowPlane

/**
 * A custom [ListView] that implements [ShadowsViewGroup].
 *
 * Apart from the additional handling of the library's shadow properties and
 * draw operations, this group behaves just like its base class.
 */
public class ShadowsListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = android.R.attr.listViewStyle,
    defStyleRes: Int = 0
) : ListView(context, attrs, defStyleAttr, defStyleRes), ShadowsViewGroup {

    internal val manager = RecyclingManager(
        this,
        attrs,
        this::attachViewToParent,
        this::detachAllViewsFromParent,
        { c -> super.dispatchDraw(c) },
        { c, v, t -> super.drawChild(c, v, t) }
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

    override fun dispatchDraw(canvas: Canvas) {
        manager.dispatchDraw(canvas)
    }

    override fun drawChild(
        canvas: Canvas,
        child: View,
        drawingTime: Long
    ): Boolean = manager.drawChild(canvas, child, drawingTime)
}