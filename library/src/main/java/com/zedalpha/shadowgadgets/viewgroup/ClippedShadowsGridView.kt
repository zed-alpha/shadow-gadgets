@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.graphics.Canvas
import android.os.Build
import android.util.AttributeSet
import android.view.View
import android.widget.GridView
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.R

class ClippedShadowsGridView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = R.style.Widget_ShadowGadgets_ClippedShadowsGridView
) : GridView(context, attrs, defStyleAttr, defStyleRes), ClippedShadowsContainer {

    private val manager = RecyclerContainerManager(this, attrs)

    override val isUsingShadowsFallback = manager.isUsingFallback

    override fun onViewAdded(child: View) {
        super.onViewAdded(child)
        manager.onViewAdded(child)
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