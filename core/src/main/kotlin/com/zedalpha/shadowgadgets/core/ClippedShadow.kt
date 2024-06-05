package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.annotation.RequiresApi

class ClippedShadow private constructor(
    private val shadow: Shadow
) : Shadow by shadow {

    constructor(
        ownerView: View,
        forceViewType: Boolean = false
    ) : this(Shadow(ownerView, forceViewType))

    @RequiresApi(29)
    constructor() : this(Shadow())

    private val clipPath = Path()

    var pathProvider: PathProvider? = null

    override fun setOutline(outline: Outline) {
        shadow.setOutline(outline)

        val path = clipPath
        path.rewind()
        if (outline.isEmpty) return

        val bounds = tmpRect
        if (getOutlineRect(outline, bounds) && !bounds.isEmpty) {
            val radius = getOutlineRadius(outline)
            val boundsF = tmpRectF
            boundsF.set(bounds)
            path.addRoundRect(boundsF, radius, radius, Path.Direction.CW)
            return
        }

        if (OutlinePathReflector.getPath(path, outline)) return
        pathProvider?.getPath(path)
    }

    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated || clipPath.isEmpty) return

        val shadow = shadow
        val matrix = tmpMatrix
        val path = tmpPath

        getMatrix(matrix)
        matrix.postTranslate(shadow.left.toFloat(), shadow.top.toFloat())
        clipPath.transform(matrix, path)

        canvas.save()
        clipOutPath(canvas, path)
        shadow.draw(canvas)
        canvas.restore()
    }

    private val tmpRect = Rect()

    private val tmpRectF = RectF()

    private val tmpMatrix = Matrix()

    private val tmpPath = Path()
}

fun interface PathProvider {
    fun getPath(path: Path)
}