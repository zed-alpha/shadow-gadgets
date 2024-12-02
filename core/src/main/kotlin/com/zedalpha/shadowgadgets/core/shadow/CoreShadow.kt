package com.zedalpha.shadowgadgets.core.shadow

import android.graphics.Canvas
import android.graphics.Outline
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.disableZ
import com.zedalpha.shadowgadgets.core.enableZ

internal abstract class CoreShadow : Shadow {

    protected val outline = Outline()

    @CallSuper
    override fun setOutline(outline: Outline) =
        this.outline.set(outline)

    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated) return

        enableZ(canvas)
        onDraw(canvas)
        disableZ(canvas)
    }

    protected abstract fun onDraw(canvas: Canvas)
}