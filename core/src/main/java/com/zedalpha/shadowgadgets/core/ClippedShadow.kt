package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

sealed class ClippedShadow : Shadow {

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

    protected val shadowOutline = Outline()

    private val clipPath = Path()

    private val tmpRect = Rect()

    private val tmpRectF = RectF()

    private val tmpPath = Path()

    private val tmpMatrix = Matrix()

    var pathProvider: PathProvider? = null

    @CallSuper
    override fun setOutline(outline: Outline?) {
        val clipPath = clipPath
        val shadowOutline = shadowOutline

        clipPath.reset()
        if (outline != null) {
            shadowOutline.set(outline)
            val bounds = tmpRect
            if (getOutlineRect(shadowOutline, bounds) && !bounds.isEmpty) {
                val boundsF = tmpRectF; boundsF.set(bounds)
                val outlineRadius = getOutlineRadius(shadowOutline)
                clipPath.addRoundRect(
                    boundsF,
                    outlineRadius,
                    outlineRadius,
                    Path.Direction.CW
                )
            } else if (!OutlinePathReflector.getPath(clipPath, shadowOutline)) {
                pathProvider?.getPath(clipPath)
            }
        } else {
            shadowOutline.setEmpty()
        }
    }

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
        enableZ(canvas)
        onDraw(canvas)
        disableZ(canvas)
        canvas.restore()
    }

    protected abstract fun onDraw(canvas: Canvas)

    @CallSuper
    open fun dispose() {
        pathProvider = null
    }
}