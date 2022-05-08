@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zedalpha.shadowgadgets.viewgroup.ClippedShadowsViewGroup.ShadowPlane

class ClippedShadowsRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RecyclerView(context, attrs), ClippedShadowsViewGroup {
    private val manager = RecyclingManager(this, attrs)

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
        /* No-op for Recycling ViewGroups.*/
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
}