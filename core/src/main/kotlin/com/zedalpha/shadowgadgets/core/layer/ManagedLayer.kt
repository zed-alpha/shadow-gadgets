package com.zedalpha.shadowgadgets.core.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build

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

fun interface LayerDraw {
    fun draw(canvas: Canvas)
}

val DefaultInlineLayerRequired = Build.VERSION.SDK_INT in 24..28