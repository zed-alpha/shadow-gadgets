package com.zedalpha.shadowgadgets.view.internal

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF

internal class GraphicsTemps {
    val rect: Rect = Rect()
    val rectF: RectF = RectF()
    val matrix: Matrix = Matrix()
    val path: Path = Path()
}

internal val ThreadLocalGraphicsTemps: GraphicsTemps
    get() = GraphicsTempsThreadLocal.get()
        ?: GraphicsTemps().also { GraphicsTempsThreadLocal.set(it) }

private val GraphicsTempsThreadLocal = ThreadLocal<GraphicsTemps>()