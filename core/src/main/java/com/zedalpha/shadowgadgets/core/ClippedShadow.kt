package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Region
import android.os.Build
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

sealed class ClippedShadow : Shadow {

    protected val outline = Outline()

    private val clipPath = Path()

    private val tempRect = Rect()

    private val tmpRectF = RectF()

    private val tmpMatrix = Matrix()

    @CallSuper
    override fun setOutline(outline: Outline?) {
        if (outline != null) {
            this.outline.set(outline)
        } else {
            this.outline.setEmpty()
        }
    }

    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated || isClipEmpty()) return

        val matrix = if (!hasIdentityMatrix()) {
            tmpMatrix.also { getMatrix(it) }
        } else {
            null
        }

        canvas.save()
        clip(canvas, matrix)
        enableZ(canvas)
        onDraw(canvas)
        disableZ(canvas)
        canvas.restore()
    }

    protected abstract fun onDraw(canvas: Canvas)

    var pathProvider: PathProvider? = null

    @CallSuper
    open fun dispose() {
        pathProvider = null
    }

    private fun isClipEmpty(): Boolean {
        val path = clipPath
        val outline = outline

        path.reset()
        if (!outline.isEmpty) {
            val bounds = tempRect
            if (getOutlineRect(outline, bounds) && !bounds.isEmpty) {
                val boundsF = tmpRectF.apply { set(bounds) }
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
        return path.isEmpty
    }

    private fun getOutlineRect(outline: Outline, outRect: Rect) =
        if (Build.VERSION.SDK_INT >= 24) {
            OutlineRect24.getRect(outline, outRect)
        } else {
            OutlineRectReflector.getRect(outline, outRect)
        }

    private fun getOutlineRadius(outline: Outline): Float =
        if (Build.VERSION.SDK_INT >= 24) {
            OutlineRect24.getRadius(outline)
        } else {
            OutlineRectReflector.getRadius(outline)
        }

    private fun enableZ(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= 29) {
            CanvasZ29.enableZ(canvas)
        } else {
            CanvasZReflector.enableZ(canvas)
        }
    }

    private fun clip(canvas: Canvas, matrix: Matrix?) {
        if (matrix != null) clipPath.transform(matrix)
        if (Build.VERSION.SDK_INT >= 26) {
            CanvasClip26.clipOutPath(canvas, clipPath)
        } else {
            @Suppress("DEPRECATION")
            canvas.clipPath(clipPath, Region.Op.DIFFERENCE)
        }
    }

    private fun disableZ(canvas: Canvas) {
        if (Build.VERSION.SDK_INT >= 29) {
            CanvasZ29.disableZ(canvas)
        } else {
            CanvasZReflector.disableZ(canvas)
        }
    }

    companion object {

        fun newInstance(
            ownerView: View,
            forceViewType: Boolean = false
        ) = if (RenderNodeFactory.isOpenForBusiness && !forceViewType) {
            RenderNodeClippedShadow()
        } else {
            ViewClippedShadow(ownerView)
        }

        @RequiresApi(29)
        fun newInstance(): ClippedShadow = RenderNodeClippedShadow()
    }
}