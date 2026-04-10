package com.zedalpha.shadowgadgets.view.plane

import android.graphics.Canvas
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.core.graphics.withClip
import com.zedalpha.shadowgadgets.view.internal.BaseDrawable
import com.zedalpha.shadowgadgets.view.internal.OnLayoutChangeSizeAdapter

internal open class OverlayDrawable(
    protected val viewGroup: ViewGroup,
    private val content: (Canvas) -> Unit
) : BaseDrawable() {

    private val updateBounds =
        OnLayoutChangeSizeAdapter { w, h -> superSetBounds(0, 0, w, h) }

    @CallSuper
    open fun attach() {
        val group = viewGroup
        group.overlay.add(this)
        group.addOnLayoutChangeListener(updateBounds)
        superSetBounds(0, 0, group.width, group.height)
    }

    @CallSuper
    open fun detach() {
        viewGroup.overlay.remove(this)
        viewGroup.removeOnLayoutChangeListener(updateBounds)
    }

    @CallSuper
    override fun draw(canvas: Canvas) =
        with(viewGroup) {
            if (clipToPadding &&
                (paddingLeft != 0 || paddingTop != 0 ||
                        paddingRight != 0 || paddingBottom != 0)
            ) {
                canvas.withClip(
                    left = paddingLeft,
                    top = paddingTop,
                    right = width - paddingRight,
                    bottom = height - paddingBottom,
                    block = content
                )
            } else {
                content(canvas)
            }
        }
}