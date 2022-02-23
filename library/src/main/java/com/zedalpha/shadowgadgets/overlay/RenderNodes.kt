@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.DisplayListCanvas
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.rendernode.CanvasReflector
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper


internal class RenderNodeController(override val viewGroup: ViewGroup) :
    OverlayController<RenderNodeShadow> {

    override val shadows = mutableListOf<RenderNodeShadow>()

    override fun createShadow(view: View) = RenderNodeShadow(view, this)
}

internal class RenderNodeShadow(
    view: View,
    private val controller: RenderNodeController
) : OverlayShadow(view, controller) {

    private val renderNode = RenderNodeFactory.newInstance()
    private val drawable = RenderNodeDrawable()

    override fun setOutline(outline: Outline) {
        super.setOutline(outline)
        renderNode.setOutline(outline)
    }

    override fun attachShadow() {
        controller.viewGroup.overlay.add(drawable)
    }

    override fun detachShadow() {
        controller.viewGroup.overlay.remove(drawable)
    }

    override fun onPreDraw() {
        if (drawable.updateShadow()) drawable.invalidateSuper()
    }

    inner class RenderNodeDrawable : Drawable() {
        override fun draw(canvas: Canvas) {
            val target = targetView
            val path = CachePath
            val boundsF = CacheBoundsF

            if (this@RenderNodeShadow.willDraw) {
                updateShadow()

                boundsF.set(outlineBounds)
                path.rewind()
                path.addRoundRect(boundsF, outlineRadius, outlineRadius, Path.Direction.CW)

                val node = renderNode
                if (!node.hasIdentityMatrix()) {
                    val matrix = CacheMatrix
                    node.getMatrix(matrix)
                    path.transform(matrix)
                }

                path.offset(target.left.toFloat(), target.top.toFloat())

                clipAndDraw(canvas, renderNode, path)
            }
        }

        fun invalidateSuper() {
            super.invalidateSelf()
        }

        override fun invalidateSelf() {
            // To stop invalidate call from bounds change; we'll handle it.
        }

        fun updateShadow(): Boolean {
            val node = renderNode
            val target = targetView

            val left = target.left
            val top = target.top
            val right = target.right
            val bottom = target.bottom

            // Possibly unnecessary for proper invalidation.
            setBounds(left, top, right, bottom)

            // No invalidation for these.
            node.setAlpha(target.alpha)
            node.setCameraDistance(target.cameraDistance)
            node.setElevation(target.elevation)
            node.setRotationX(target.rotationX)
            node.setRotationY(target.rotationY)
            node.setRotationZ(target.rotation)
            node.setTranslationZ(target.translationZ)

            return node.setPivotX(target.pivotX) or
                    node.setPivotY(target.pivotY) or
                    node.setPosition(left, top, right, bottom) or
                    node.setScaleX(target.scaleX) or
                    node.setScaleY(target.scaleY) or
                    node.setTranslationX(target.translationX) or
                    node.setTranslationY(target.translationY)
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
internal val UsesPublicApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
internal val UsesStubs = Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.O_MR1

@SuppressLint("NewApi")
private val clipAndDraw: (canvas: Canvas, wrapper: RenderNodeWrapper, path: Path) -> Unit =
    when {
        UsesPublicApi -> { canvas, wrapper, path ->
            canvas.save()
            canvas.enableZ()
            canvas.clipOutPath(path)
            wrapper.draw(canvas)
            canvas.disableZ()
            canvas.restore()
        }
        UsesStubs -> { canvas, wrapper, path ->
            canvas as DisplayListCanvas
            canvas.save()
            clipOutPath(canvas, path)
            canvas.insertReorderBarrier()
            wrapper.draw(canvas)
            canvas.insertInorderBarrier()
            canvas.restore()
        }
        else -> { canvas, wrapper, path ->
            canvas.save()
            clipOutPath(canvas, path)
            CanvasReflector.insertReorderBarrier(canvas)
            wrapper.draw(canvas)
            CanvasReflector.insertInorderBarrier(canvas)
            canvas.restore()
        }
    }