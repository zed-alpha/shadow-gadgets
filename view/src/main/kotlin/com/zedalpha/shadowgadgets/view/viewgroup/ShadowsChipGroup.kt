package com.zedalpha.shadowgadgets.view.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.google.android.material.chip.ChipGroup
import com.zedalpha.shadowgadgets.view.ShadowPlane

/**
 * A custom [ChipGroup] that implements [ShadowsViewGroup].
 *
 * Apart from the additional handling of the library's shadow properties and
 * draw operations, this group behaves just like its base class.
 */
public class ShadowsChipGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.chipGroupStyle
) : ChipGroup(context, attrs, defStyleAttr), ShadowsViewGroup {

    internal val manager = RegularManager(
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

    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        manager.generateLayoutParams(attrs)
        return super.generateLayoutParams(attrs)
    }

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