package com.zedalpha.shadowgadgets.view.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.zedalpha.shadowgadgets.view.ShadowPlane

/**
 * A custom [ConstraintLayout] that implements [ShadowsViewGroup].
 *
 * Apart from the additional handling of the library's shadow properties and
 * draw operations, this group behaves just like its base class.
 */
public open class ShadowsConstraintLayout
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes),
    ShadowsViewGroup {

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
        get() = manager.childShadowsPlane
        set(value) {
            manager.childShadowsPlane = value
        }

    override var clipAllChildShadows: Boolean
        get() = manager.clipAllChildShadows
        set(value) {
            manager.clipAllChildShadows = value
        }

    override var childOutlineShadowsColorCompat: Int
        get() = manager.childOutlineShadowsColorCompat
        set(value) {
            manager.childOutlineShadowsColorCompat = value
        }

    override var forceChildOutlineShadowsColorCompat: Boolean
        get() = manager.forceChildOutlineShadowsColorCompat
        set(value) {
            manager.forceChildOutlineShadowsColorCompat = value
        }

    override var takeOverDrawForInlineChildShadows: Boolean
        get() = manager.takeOverDrawForInlineChildShadows
        set(value) {
            manager.takeOverDrawForInlineChildShadows = value
        }

    @Deprecated(
        "Use takeOverDrawForInlineChildShadows " +
                "instead. It has opposite but clearer semantics."
    )
    override var ignoreInlineChildShadows: Boolean
        get() = manager.ignoreInlineChildShadows
        set(value) {
            manager.ignoreInlineChildShadows = value
        }

    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
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