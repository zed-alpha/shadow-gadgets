package com.zedalpha.shadowgadgets.core.layer

import android.graphics.Canvas
import android.graphics.Paint

internal interface ManagedLayer {

    val drawContent: (Canvas) -> Unit

    fun setSize(width: Int, height: Int)

    fun setLayerPaint(paint: Paint)

    fun draw(canvas: Canvas)

    fun recreate()

    fun dispose()

    fun invalidate() {}

    fun refresh() {}
}