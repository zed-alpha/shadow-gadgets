package com.zedalpha.shadowgadgets.view.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import com.google.android.material.R
import com.google.android.material.chip.ChipGroup
import com.zedalpha.shadowgadgets.view.ShadowPlane

/**
 * A custom [ChipGroup] that implements [ShadowsViewGroup].
 *
 * Apart from the additional handling of the library's shadow properties and
 * draw operations, this group behaves just like its base class.
 */
public open class ShadowsChipGroup
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.chipGroupStyle
) : ChipGroup(context, attrs, defStyleAttr), ShadowsViewGroup {

    internal val manager =
        RegularManager(
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

    override var takeOverDrawForInlineChildShadows: Boolean
            by manager::takeOverDrawForInlineChildShadows

    @Deprecated(
        "Use takeOverDrawForInlineChildShadows " +
                "instead. It has opposite but clearer semantics."
    )
    override var ignoreInlineChildShadows: Boolean
            by manager::ignoreInlineChildShadows

    @Suppress("RemoveRedundantQualifierName")
    override fun generateLayoutParams(attrs: AttributeSet?): ViewGroup.LayoutParams {
        manager.generateLayoutParams(attrs)
        return super.generateLayoutParams(attrs)
    }

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