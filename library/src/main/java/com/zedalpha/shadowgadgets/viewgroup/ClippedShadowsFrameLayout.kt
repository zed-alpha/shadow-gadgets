@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.ClippedShadowPlane

class ClippedShadowsFrameLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsViewGroup {
    private val manager = ViewGroupShadowManager(this, attrs)

    override val isUsingShadowsFallback = manager.isUsingFallback

    override var clipAllChildShadows by manager::clipAllChildShadows
    override var childClippedShadowsPlane by manager::childClippedShadowsPlane
    override var disableChildShadowsOnFallback by manager::disableChildShadowsOnFallback

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams) = p is LayoutParams

    override fun generateDefaultLayoutParams() =
        LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    override fun generateLayoutParams(attrs: AttributeSet?) = LayoutParams(context, attrs)

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?) = LayoutParams(lp)

    class LayoutParams : FrameLayout.LayoutParams, ClippedShadowsLayoutParams {
        override var clipOutlineShadow: Boolean? = null
        override var clippedShadowPlane: ClippedShadowPlane? = null
        override var disableShadowOnFallback: Boolean? = null

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