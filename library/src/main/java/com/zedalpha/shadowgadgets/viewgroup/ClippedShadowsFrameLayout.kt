@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.viewgroup.ClippedShadowsViewGroup.ShadowPlane

class ClippedShadowsFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = RegularManager(this, attrs)

    override val isUsingShadowsFallback = manager.isUsingFallback

    override var clipAllChildShadows by manager::clipAllChildShadows
    override var disableChildShadowsOnFallback by manager::disableChildShadowsOnFallback
    override var childClippedShadowPlane by manager::childClippedShadowPlane

    override fun setChildClipOutlineShadow(
        child: View,
        clipOutlineShadow: Boolean,
        disableShadowOnFallback: Boolean,
        @ShadowPlane clippedShadowPlane: Int
    ) {
        manager.setChildClipOutlineShadow(
            child,
            clipOutlineShadow,
            disableShadowOnFallback,
            clippedShadowPlane
        )
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        manager.onViewRemoved(child)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        manager.onAttachedToWindow()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        manager.onDetachedFromWindow()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        manager.onSizeChanged(w, h)
    }

    override fun dispatchDraw(canvas: Canvas) {
        manager.wrapDispatchDraw(canvas) { super.dispatchDraw(canvas) }
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams) = p is LayoutParams

    override fun generateDefaultLayoutParams() =
        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    override fun generateLayoutParams(attrs: AttributeSet?) = LayoutParams(context, attrs)

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?) = LayoutParams(lp)

    class LayoutParams : FrameLayout.LayoutParams, ClippedShadowsLayoutParams {
        override var clipOutlineShadow: Boolean = false
        override var disableShadowOnFallback: Boolean = false
        override var clippedShadowPlane: Int = ClippedShadowsViewGroup.FOREGROUND

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            attrs.extractClippedShadowsLayoutParamsValues(context, this)
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(width: Int, height: Int, gravity: Int) : super(width, height, gravity)
        constructor(source: ViewGroup.LayoutParams) : super(source)
        constructor(source: MarginLayoutParams) : super(source)
        constructor(source: LayoutParams) : super(source) {
            this.clipOutlineShadow = source.clipOutlineShadow
            this.disableShadowOnFallback = source.disableShadowOnFallback
            this.clippedShadowPlane = source.clippedShadowPlane
        }
    }
}