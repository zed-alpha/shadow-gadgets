package com.zedalpha.shadowgadgets.view.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * A custom [RecyclerView] that implements [ShadowsViewGroup].
 *
 * Apart from the additional handling of the library's shadow properties and
 * draw operations, this group behaves just like its base class.
 */
class ShadowsRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs), ShadowsViewGroup {

    internal val manager = RecyclingManager(
        this,
        attrs,
        this::attachViewToParent,
        this::detachAllViewsFromParent,
        { c -> super.dispatchDraw(c) },
        { c, v, t -> super.drawChild(c, v, t) }
    )

    override var childShadowsPlane by manager::childShadowsPlane

    override var clipAllChildShadows by manager::clipAllChildShadows

    override var childOutlineShadowsColorCompat
            by manager::childOutlineShadowsColorCompat

    override var forceChildOutlineShadowsColorCompat
            by manager::forceChildOutlineShadowsColorCompat

    override var ignoreInlineChildShadows by manager::ignoreInlineChildShadows

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