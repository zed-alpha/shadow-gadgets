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

    private val tmpRect = Rect()

    private val tmpRectF = RectF()

    private val tmpMatrix = Matrix()

    private val tmpPath = Path()

    override fun setOutline(outline: Outline) {
        shadow.setOutline(outline)
        calculateClipPath(clipPath, shadow.outline)
    }

    var pathProvider: PathProvider? = null

    private fun calculateClipPath(path: Path, outline: Outline) {
        path.reset()
        if (!outline.isEmpty) {
            val bounds = tmpRect
            if (getOutlineRect(outline, bounds) && !bounds.isEmpty) {
                val outlineRadius = getOutlineRadius(outline)
                path.addRoundRect(
                    tmpRectF.apply { set(bounds) },
                    outlineRadius,
                    outlineRadius,
                    Path.Direction.CW
                )
            } else if (!OutlinePathReflector.getPath(path, outline)) {
                pathProvider?.getPath(path)
            }
        }
    }

    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated || clipPath.isEmpty) return

        if (hasIdentityMatrix()) {
            tmpMatrix.reset()
        } else {
            getMatrix(tmpMatrix)
        }
        tmpMatrix.postTranslate(shadow.left.toFloat(), shadow.top.toFloat())
        clipPath.transform(tmpMatrix, tmpPath)

        canvas.save()
        clipOutPath(canvas, tmpPath)
        shadow.draw(canvas)
        canvas.restore()
    }
}

fun interface PathProvider {
    fun getPath(path: Path)
}