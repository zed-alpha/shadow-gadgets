@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.Region
import android.os.Build
import android.view.DisplayListCanvas
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.rendernode.CanvasReflector
import com.zedalpha.shadowgadgets.rendernode.RenderNodeColors
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper


@RequiresApi(Build.VERSION_CODES.P)
internal object ShadowColorsHelper {
    @DoNotInline
    fun testColors(renderNode: RenderNodeWrapper) {
        renderNode as RenderNodeColors
        renderNode.setAmbientShadowColor(renderNode.getAmbientShadowColor())
        renderNode.setSpotShadowColor(renderNode.getSpotShadowColor())
    }

    @DoNotInline
    fun changeColors(renderNode: RenderNodeWrapper, target: View): Boolean {
        renderNode as RenderNodeColors
        return renderNode.setAmbientShadowColor(target.outlineAmbientShadowColor) or
                renderNode.setSpotShadowColor(target.outlineSpotShadowColor)
    }

    @DoNotInline
    fun changeColors(shadow: View, target: View): Boolean {
        var changed = false
        if (shadow.outlineAmbientShadowColor != target.outlineAmbientShadowColor) {
            shadow.outlineAmbientShadowColor = target.outlineAmbientShadowColor
            changed = true
        }
        if (shadow.outlineSpotShadowColor != target.outlineSpotShadowColor) {
            shadow.outlineSpotShadowColor = target.outlineSpotShadowColor
            changed = true
        }
        return changed
    }
}

@SuppressLint("ViewConstructor")
internal class ViewShadowContainer(context: Context, private val shadows: List<ViewShadow>) :
    ViewGroup(context), View.OnLayoutChangeListener {

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        shadows.forEach { shadow ->
            if (shadow.willDraw) {
                shadow.updateShadow()
                clipOutPath(canvas, shadow.calculateClipPath())
            }
        }
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun addView(child: View?) {
        addView(child, EmptyLayoutParams)
    }

    fun detachShadowView(child: ViewShadow.ShadowView): Int {
        val index = indexOfChild(child)
        detachViewFromParent(child)
        return index
    }

    fun reAttachShadowView(child: ViewShadow.ShadowView, index: Int) {
        attachViewToParent(child, index, EmptyLayoutParams)
    }

    override fun onLayoutChange(
        v: View, l: Int, t: Int, r: Int, b: Int, ol: Int, ot: Int, or: Int, ob: Int
    ) {
        val newWidth = r - l
        val newHeight = b - t
        if (width != newWidth || height != newHeight) {
            layout(0, 0, newWidth, newHeight)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        /* no-op. Child layout is handled manually elsewhere. */
    }

    private object EmptyLayoutParams : ViewGroup.LayoutParams(0, 0)
}

private val clipOutPath: (Canvas, Path) -> Unit =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { canvas, path ->
        canvas.clipOutPath(path)
    } else { canvas, path ->
        @Suppress("DEPRECATION")
        canvas.clipPath(path, Region.Op.DIFFERENCE)
    }

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val UsesPublicApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
private val UsesStubs = Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.O_MR1

@SuppressLint("NewApi")
internal val clipAndDraw: (canvas: Canvas, path: Path, wrapper: RenderNodeWrapper) -> Unit =
    when {
        UsesPublicApi -> { canvas, path, wrapper ->
            canvas.save()
            canvas.enableZ()
            canvas.clipOutPath(path)
            wrapper.draw(canvas)
            canvas.disableZ()
            canvas.restore()
        }
        UsesStubs -> { canvas, path, wrapper ->
            canvas as DisplayListCanvas
            canvas.save()
            clipOutPath(canvas, path)
            canvas.insertReorderBarrier()
            wrapper.draw(canvas)
            canvas.insertInorderBarrier()
            canvas.restore()
        }
        else -> { canvas, path, wrapper ->
            canvas.save()
            clipOutPath(canvas, path)
            CanvasReflector.insertReorderBarrier(canvas)
            wrapper.draw(canvas)
            CanvasReflector.insertInorderBarrier(canvas)
            canvas.restore()
        }
    }