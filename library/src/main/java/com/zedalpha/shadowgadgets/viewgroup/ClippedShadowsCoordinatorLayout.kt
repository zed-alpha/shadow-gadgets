@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.RequiresApi
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import com.zedalpha.shadowgadgets.shadow.RenderNodeShadow
import com.zedalpha.shadowgadgets.shadow.ViewShadow

class ClippedShadowsCoordinatorLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : CoordinatorLayout(context, attrs), ClippedShadowsContainer {

    private val manager = StandardContainerManager(this, attrs)

    override val isUsingShadowsFallback = manager.isUsingFallback

    init {
        viewTreeObserver.addOnPreDrawListener {
            children.forEach { child ->
                val shadow = child.inlineShadow
                if (shadow != null && shadow.updateShadow()) {
                    if (shadow is RenderNodeShadow) {
                        invalidate()
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
        manager.onViewAdded(child, false)
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
}