@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.*
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.rendernode.CanvasReflector
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper


internal class RenderNodeController(private val viewGroup: ViewGroup) :
    OverlayController<RenderNodeShadow>, ViewTreeObserver.OnPreDrawListener {
    private val shadows = mutableListOf<RenderNodeShadow>()

    override fun attach() {
        viewGroup.setTag(R.id.tag_parent_overlay_shadow_controller, this)
        viewGroup.viewTreeObserver.addOnPreDrawListener(this)
    }

    private fun detach() {
        viewGroup.setTag(R.id.tag_parent_overlay_shadow_controller, null)
        viewGroup.viewTreeObserver.removeOnPreDrawListener(this)
    }

    override fun newShadow(view: View) {
        val shadow = RenderNodeShadow(view, this)
        shadow.attachToTargetView()
        shadows.forEach { it.removeFromOverlay(viewGroup.overlay) }
        shadows.add(shadow)
        shadows.sortedBy { it.z }.forEach { it.addToOverlay(viewGroup.overlay) }
    }

    override fun removeShadow(view: View) {
        val shadow = view.getTag(R.id.tag_target_overlay_shadow) as RenderNodeShadow
        shadow.detachFromTargetView()
        shadow.removeFromOverlay(viewGroup.overlay)
        shadows.remove(shadow)
        if (shadows.isEmpty()) detach()
    }

    override fun onPreDraw(): Boolean {
        shadows.forEach { it.updateShadow() }
        return true
    }
}

internal class RenderNodeShadow(
    private val targetView: View,
    private val controller: OverlayController<*>
) : OverlayShadow {

    private val outlineBounds = Rect()
    private var radius: Float = 0.0F

    private val originalProvider: ViewOutlineProvider = targetView.outlineProvider

    private val drawable = when {
        UsesPublicApi -> PublicApiDrawable(this)
        UsesStubs -> StubDrawable(this)
        else -> ReflectorDrawable(this)
    }

    fun addToOverlay(overlay: ViewOverlay) {
        overlay.add(drawable)
    }

    fun removeFromOverlay(overlay: ViewOverlay) {
        overlay.remove(drawable)
    }

    override fun attachToTargetView() {
        val target = targetView
        target.setTag(R.id.tag_target_overlay_shadow, this)
        target.outlineProvider = ProviderWrapper(originalProvider, this::setOutline)
    }

    override fun detachFromTargetView() {
        val target = targetView
        target.setTag(R.id.tag_target_overlay_shadow, null)
        target.outlineProvider = originalProvider
    }

    override fun detachFromController() {
        controller.removeShadow(targetView)
    }

    @Suppress("LiftReturnOrAssignment")
    fun setOutline(outline: Outline) {
        if (getRect(outline, outlineBounds)) {
            // outlineBounds set in getRect()
            radius = getRadius(outline)
        } else {
            outlineBounds.setEmpty()
            radius = 0.0F
        }
        drawable.setOutline(if (outline.isEmpty) null else outline)
    }

    override val z get() = targetView.z

    @Suppress("LiftReturnOrAssignment")
    fun prepareForClip(path: Path, boundsF: RectF): Boolean {
        if (!outlineBounds.isEmpty) {
            updateShadow()
            // Set path for clip.
            boundsF.set(outlineBounds)
            boundsF.offset(targetView.left.toFloat(), targetView.top.toFloat())
            path.rewind()
            path.addRoundRect(boundsF, radius, radius, Path.Direction.CW)
            return true
        } else {
            return false
        }
    }

    fun updateShadow() {
        val target = targetView
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