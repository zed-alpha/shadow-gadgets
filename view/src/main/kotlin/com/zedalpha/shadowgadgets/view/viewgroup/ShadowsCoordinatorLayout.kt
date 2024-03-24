@file:Suppress("DEPRECATION")

package com.zedalpha.shadowgadgets.view.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.View
import androidx.annotation.CallSuper
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.zedalpha.shadowgadgets.view.ShadowPlane

/**
 * A custom [CoordinatorLayout] that implements [ShadowsViewGroup].
 *
 * Apart from the additional handling of the library's shadow properties and
 * draw operations, this group behaves just like its base class.
 *
 * Currently, this class directly extends its deprecated `Clipped` predecessor
 * in order to keep everything working without making breaking changes during
 * the transition. User code should not expect or rely on this fact, as the
 * `Clipped*` classes will eventually be removed altogether.
 *
 * Also, due to this inheritance, and the settings chosen to generate minimal
 * documentation, some of [ShadowsViewGroup]'s members do not show here.
 */
class ShadowsCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ClippedShadowsCoordinatorLayout(context, attrs, defStyleAttr),
    ShadowsViewGroup {

    override var childShadowsPlane by manager::childShadowsPlane

    @Deprecated(
        "Replaced by childShadowsPlane",
        ReplaceWith("childShadowsPlane")
    )
    override var childClippedShadowsPlane: ShadowPlane
        get() = super.childClippedShadowsPlane
        set(value) {
            super.childClippedShadowsPlane = value
        }

    override var childOutlineShadowsColorCompat
            by manager::childOutlineShadowsColorCompat

    override var forceChildOutlineShadowsColorCompat
            by manager::forceChildOutlineShadowsColorCompat
}

/**
 * Replaced by [ShadowsCoordinatorLayout]
 */
@Deprecated(
    "Replaced by ShadowsCoordinatorLayout",
    ReplaceWith("ShadowsCoordinatorLayout")
)
open class ClippedShadowsCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr), ClippedShadowsViewGroup {

    @Suppress("LeakingThis")
    internal val manager = RegularManager(
        this,
        attrs,
        this::detachAllViewsFromParent,
        this::attachViewToParent
    ) { super.dispatchDraw(it) }

    @get:CallSuper
    @set:CallSuper
    override var clipAllChildShadows by manager::clipAllChildShadows

    @get:CallSuper
    @set:CallSuper
    override var childClippedShadowsPlane by manager::childShadowsPlane

    @get:CallSuper
    @set:CallSuper
    override var ignoreInlineChildShadows by manager::ignoreInlineChildShadows

    @CallSuper
    override fun generateLayoutParams(attrs: AttributeSet?): LayoutParams {
        manager.generateLayoutParams(attrs)
        return super.generateLayoutParams(attrs)
    }

    @CallSuper
    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }

    @CallSuper
    override fun dispatchDraw(canvas: Canvas) {
        manager.dispatchDraw(canvas)
    }

    @CallSuper
    override fun drawChild(
        canvas: Canvas,
        child: View,
        drawingTime: Long
    ): Boolean = manager.drawChild(canvas, child) {
        super.drawChild(canvas, child, drawingTime)
    }
}