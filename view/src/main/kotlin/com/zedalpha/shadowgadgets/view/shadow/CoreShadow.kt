package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import com.zedalpha.shadowgadgets.view.internal.disableZ
import com.zedalpha.shadowgadgets.view.internal.enableZ

internal abstract class CoreShadow : Shadow {

    final override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated) return

        enableZ(canvas)
        onDraw(canvas)
        disableZ(canvas)
    }

    protected abstract fun onDraw(canvas: Canvas)
}