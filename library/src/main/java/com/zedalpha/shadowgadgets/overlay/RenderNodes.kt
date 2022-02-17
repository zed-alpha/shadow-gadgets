package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.DisplayListCanvas
import android.view.View
import android.view.ViewGroup
import android.view.ViewOverlay
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.rendernode.CanvasReflector
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("ViewConstructor")
internal class RenderNodeController(override val viewGroup: ViewGroup) :
    OverlayController<RenderNodeShadow> {

    override val shadows = mutableListOf<RenderNodeShadow>()

    override fun createShadow(view: View) = RenderNodeShadow(view, this)

    override fun newShadow(view: View) {
        super.newShadow(view)
        shadows.sortedBy { it.z }.forEach { it.addToOverlay(viewGroup.overlay) }
    }

    override fun onNewShadow(shadow: RenderNodeShadow) {
        shadows.forEach { it.removeFromOverlay(viewGroup.overlay) }
    }

    override fun onRemoveShadow(shadow: RenderNodeShadow) {
        shadow.removeFromOverlay(viewGroup.overlay)
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class RenderNodeShadow(view: View, controller: RenderNodeController) :
    OverlayShadow(view, controller) {
    private val drawable = when {
        UsesPublicApi -> PublicApiDrawable(this)
        UsesStubs -> StubDrawable(this)
        else -> ReflectorDrawable(this)
    }

    override fun setOutline(outline: Outline) {
        super.setOutline(outline)
        drawable.setOutline(if (outline.isEmpty) null else outline)
    }

    fun addToOverlay(overlay: ViewOverlay) {
        overlay.add(drawable)
    }

    fun removeFromOverlay(overlay: ViewOverlay) {
        overlay.remove(drawable)
    }

    fun setElevation(elevation: Float) {
        drawable.setElevation(elevation)
    }

    fun setTranslationZ(translationZ: Float) {
        drawable.setTranslationZ(translationZ)
    }

    override fun updateShadow(target: View) {
        drawable.setVisible(target.isVisible, false)
        drawable.setRenderNodeAlpha(target.alpha)
        drawable.setCameraDistance(target.cameraDistance)
        drawable.setElevation(target.elevation)
        drawable.setPivotX(target.pivotX)
        drawable.setPivotY(target.pivotY)
        drawable.setBounds(target.left, target.top, target.right, target.bottom)
        drawable.setRotationX(target.rotationX)
        drawable.setRotationY(target.rotationY)
        drawable.setRotationZ(target.rotation)
        drawable.setScaleX(target.scaleX)
        drawable.setScaleY(target.scaleY)
        drawable.setTranslationX(target.translationX)
        drawable.setTranslationY(target.translationY)
        drawable.setTranslationZ(target.translationZ)
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private open class ReflectorDrawable(shadow: RenderNodeShadow) : RenderNodeShadowDrawable(shadow) {
    override fun clipAndDraw(canvas: Canvas, path: Path, boundsF: RectF) {
        canvas.save()
        clipOutPath(canvas, path)
        CanvasReflector.insertReorderBarrier(canvas)
        wrapper.draw(canvas)
        CanvasReflector.insertInorderBarrier(canvas)
        canvas.restore()
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private class StubDrawable(shadow: RenderNodeShadow) : RenderNodeShadowDrawable(shadow) {
    override fun clipAndDraw(canvas: Canvas, path: Path, boundsF: RectF) {
        canvas as DisplayListCanvas

        canvas.save()
        clipOutPath(canvas, path)
        canvas.insertReorderBarrier()
        wrapper.draw(canvas)
        canvas.insertInorderBarrier()
        canvas.restore()
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private class PublicApiDrawable(shadow: RenderNodeShadow) : RenderNodeShadowDrawable(shadow) {
    override fun clipAndDraw(canvas: Canvas, path: Path, boundsF: RectF) {
        canvas.save()
        canvas.enableZ()
        canvas.clipOutPath(path)
        wrapper.draw(canvas)
        canvas.disableZ()
        canvas.restore()
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal sealed class RenderNodeShadowDrawable(private val shadow: RenderNodeShadow) : Drawable() {
    protected val wrapper: RenderNodeWrapper = RenderNodeFactory.newInstance()

    fun setRenderNodeAlpha(alpha: Float) {
        wrapper.setAlpha(alpha)
    }

    fun setCameraDistance(distance: Float) {
        wrapper.setCameraDistance(distance)
    }

    fun setElevation(elevation: Float) {
        wrapper.setElevation(elevation)
    }

    fun setPivotX(pivotX: Float) {
        wrapper.setPivotX(pivotX)
    }

    fun setPivotY(pivotY: Float) {
        wrapper.setPivotY(pivotY)
    }

    fun setRotationX(rotationX: Float) {
        wrapper.setRotationX(rotationX)
    }

    fun setRotationY(rotationY: Float) {
        wrapper.setRotationY(rotationY)
    }

    fun setRotationZ(rotation: Float) {
        wrapper.setRotationZ(rotation)
    }

    fun setTranslationX(translationX: Float) {
        wrapper.setTranslationZ(translationX)
    }

    fun setTranslationY(translationY: Float) {
        wrapper.setTranslationZ(translationY)
    }

    fun setTranslationZ(translationZ: Float) {
        wrapper.setTranslationZ(translationZ)
    }

    fun setOutline(outline: Outline?) {
        wrapper.setOutline(outline)
    }

    fun setScaleX(scaleX: Float) {
        wrapper.setScaleX(scaleX)
    }

    fun setScaleY(scaleY: Float) {
        wrapper.setScaleY(scaleY)
    }

    override fun onBoundsChange(bounds: Rect) {
        wrapper.setPosition(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    final override fun draw(canvas: Canvas) {
        val path = CachePath
        val boundsF = CacheBoundsF
        if (isVisible && shadow.prepareForClip(path, boundsF)) clipAndDraw(canvas, path, boundsF)
    }

    abstract fun clipAndDraw(canvas: Canvas, path: Path, boundsF: RectF)

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(filter: ColorFilter?) {}
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val UsesPublicApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
private val UsesStubs = Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.O_MR1