@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.DisplayListCanvas
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.rendernode.CanvasReflector
import com.zedalpha.shadowgadgets.rendernode.RenderNodeColors
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper


internal var View.shadow: Shadow?
    get() = getTag(R.id.tag_target_shadow) as? Shadow
    set(value) {
        setTag(R.id.tag_target_shadow, value)
    }


internal class CallbackProviderWrapper(
    private val wrapped: ViewOutlineProvider,
    private val shadow: Shadow
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        shadow.setOutline(outline)
        outline.alpha = 0.0F
    }
}


internal class ZeroAlphaProviderWrapper(
    private val wrapped: ViewOutlineProvider
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        outline.alpha = 0.0F
    }
}


internal object EmptyDrawable : Drawable() {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(filter: ColorFilter?) {}
    override fun draw(canvas: Canvas) {}
}


internal val CachePath = Path()


internal val clipOutPath: (Canvas, Path) -> Unit =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { canvas, path ->
        canvas.clipOutPath(path)
    } else { canvas, path ->
        @Suppress("DEPRECATION")
        canvas.clipPath(path, Region.Op.DIFFERENCE)
    }


@RequiresApi(Build.VERSION_CODES.P)
internal object ShadowColorsHelper {
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