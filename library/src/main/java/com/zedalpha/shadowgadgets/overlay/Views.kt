@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.R


@SuppressLint("ViewConstructor")
internal class ViewController(private val viewGroup: ViewGroup) :
    ViewGroup(viewGroup.context), OverlayController<ViewShadow>,
    View.OnLayoutChangeListener, ViewTreeObserver.OnPreDrawListener {
    private val shadows = mutableListOf<ViewShadow>()

    init {
        val group = viewGroup
        if (group.isLaidOut) layout(0, 0, group.width, group.height)
        group.addOnLayoutChangeListener(this)
        group.overlay.add(this)
    }

    override fun attach() {
        viewGroup.setTag(R.id.tag_parent_overlay_shadow_controller, this)
        viewGroup.viewTreeObserver.addOnPreDrawListener(this)
    }

    private fun detach() {
        viewGroup.setTag(R.id.tag_parent_overlay_shadow_controller, null)
        viewGroup.viewTreeObserver.removeOnPreDrawListener(this)
        val group = viewGroup
        group.removeOnLayoutChangeListener(this)
        group.overlay.remove(this)
    }

    override fun newShadow(view: View) {
        val shadow = ViewShadow(view, this)
        shadow.attachToTargetView()
        shadow.addShadowView(this)
        shadows.add(shadow)
    }

    override fun removeShadow(view: View) {
        val shadow = view.getTag(R.id.tag_target_overlay_shadow) as ViewShadow
        shadow.detachFromTargetView()
        shadow.removeShadowView(this)
        shadows.remove(shadow)
        if (shadows.isEmpty()) detach()
    }

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        shadows.sortedBy { it.z }.forEach {
            val path = CachePath
            if (it.prepareForClip(path, CacheBoundsF)) clipOutPath(canvas, path)
        }
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
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
        /* noop. Child layout is handled manually elsewhere. */
    }

    override fun onPreDraw(): Boolean {
        shadows.forEach { it.updateShadow() }
        return true
    }

    fun detachShadowView(child: View): Int {
        val index = indexOfChild(child)
        detachViewFromParent(child)
        return index
    }

    fun reAttachShadowView(child: View, index: Int) {
        attachViewToParent(child, index, null)
    }
}

internal class ViewShadow(
    private val targetView: View,
    private val controller: ViewController
) : OverlayShadow {
    private val outlineBounds = Rect()
    private var radius: Float = 0.0F

    private val originalProvider: ViewOutlineProvider = targetView.outlineProvider

    private val shadowView = View(targetView.context).apply {
        background = EmptyDrawable
        outlineProvider = SurrogateViewProviderWrapper(originalProvider, targetView)
        clipToOutline = true
        elevation = targetView.elevation
        translationZ = targetView.translationZ
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
        val index = controller.detachShadowView(shadowView)

        shadowView.isVisible = target.isVisible
        shadowView.alpha = target.alpha
        shadowView.cameraDistance = target.cameraDistance
        shadowView.elevation = target.elevation
        shadowView.pivotX = target.pivotX
        shadowView.pivotY = target.pivotY
        shadowView.layout(target.left, target.top, target.right, target.bottom)
        shadowView.rotationX = target.rotationX
        shadowView.rotationY = target.rotationY
        shadowView.rotation = target.rotation
        shadowView.scaleX = target.scaleX
        shadowView.scaleY = target.scaleY
        shadowView.translationX = target.translationX
        shadowView.translationY = target.translationY
        shadowView.translationZ = target.translationZ

        controller.reAttachShadowView(shadowView, index)
    }

    fun addShadowView(controller: ViewController) {
        controller.addView(shadowView)
    }

    fun removeShadowView(controller: ViewController) {
        controller.removeView(shadowView)
    }
}

private object EmptyDrawable : Drawable() {
    override fun draw(canvas: Canvas) {}
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(filter: ColorFilter?) {}
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

private class SurrogateViewProviderWrapper(
    private val provider: ViewOutlineProvider,
    private val surrogate: View
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        provider.getOutline(surrogate, outline)
    }
}