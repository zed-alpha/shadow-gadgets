@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.RelativeLayout.LayoutParams
import androidx.annotation.RequiresApi
import androidx.core.view.children
import com.zedalpha.shadowgadgets.getClipOutlineShadow
import com.zedalpha.shadowgadgets.shadow.RenderNodeShadow
import com.zedalpha.shadowgadgets.shadow.ViewShadow

class ClippedShadowsRelativeLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RelativeLayout(context, attrs), ClippedShadowsContainer {

    private val manager = StandardContainerManager(this, attrs)

    override val isUsingShadowsFallback = manager.isUsingFallback

    init {
        viewTreeObserver.addOnPreDrawListener {
            children.forEach { child ->
                val shadow = child.inlineShadow
                if (shadow != null && shadow.updateShadow()) {
                    if (shadow is RenderNodeShadow) {
                        child.invalidate()
                    } else if (shadow is ViewShadow) {
                        shadow.shadowView.invalidate()
                    }
                }
            }
            true
        }
    }

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child, child.childLayoutParams.clipOutlineShadow)
    }

    override fun onViewRemoved(child: View) {
        super.onViewRemoved(child)
        manager.onViewRemoved(child)
    }

    override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
        val result = super.drawChild(canvas, child, drawingTime)
        manager.drawChild(canvas, child)
        return result
    }

    override fun generateDefaultLayoutParams() =
        LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

    override fun generateLayoutParams(attrs: AttributeSet?) = LayoutParams(context, attrs)

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?) = LayoutParams(lp)

    class LayoutParams : RelativeLayout.LayoutParams {
        internal var clipOutlineShadow: Boolean = false

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            clipOutlineShadow = context.getClipOutlineShadow(attrs)
        }

        constructor(width: Int, height: Int) : super(width, height)
        constructor(source: ViewGroup.LayoutParams) : super(source)
        constructor(source: MarginLayoutParams) : super(source)
        constructor(source: LayoutParams) : super(source) {
            this.clipOutlineShadow = source.clipOutlineShadow
        }
    }

    private val View.childLayoutParams get() = layoutParams as LayoutParams
}