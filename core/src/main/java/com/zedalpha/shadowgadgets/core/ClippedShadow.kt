package com.zedalpha.shadowgadgets.core

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.annotation.RequiresApi

class ClippedShadow private constructor(
    private val shadow: Shadow,
    private val pathProvider: PathProvider?
) : Shadow by shadow {

    constructor(
        ownerView: View,
        pathProvider: PathProvider?,
        forceViewType: Boolean = false
    ) : this(Shadow(ownerView, forceViewType), pathProvider)

    @RequiresApi(29)
    constructor(pathProvider: PathProvider?) : this(Shadow(), pathProvider)

    private val clipPath = Path()

    private val tmpRect = Rect()

    private val tmpRectF = RectF()

    private val tmpPath = Path()

    private val tmpMatrix = Matrix()

    override fun setOutline(outline: Outline?) {
        shadow.setOutline(outline)
        calculateClipPath(clipPath, shadow.outline)
    }

    private fun calculateClipPath(path: Path, outline: Outline) {
        path.reset()
        if (!outline.isEmpty) {
            val bounds = tmpRect
            if (getOutlineRect(outline, bounds) && !bounds.isEmpty) {
                val boundsF = tmpRectF; boundsF.set(bounds)
                val outlineRadius = getOutlineRadius(outline)
                path.addRoundRect(
                    boundsF,
                    outlineRadius,
                    outlineRadius,
                    Path.Direction.CW
                )
            } else if (!OutlinePathReflector.getPath(path, outline)) {
                pathProvider?.getPath(path)
            }
        }
    }

    @SuppressLint("WrongConstant")
    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated || clipPath.isEmpty) return

        if (hasIdentityMatrix()) {
            tmpPath.set(clipPath)
        } else {
            getMatrix(tmpMatrix)
            clipPath.transform(tmpMatrix, tmpPath)
        }

        canvas.save()
        clipOutPath(canvas, tmpPath)
        shadow.draw(canvas)
        canvas.restore()
    }
}