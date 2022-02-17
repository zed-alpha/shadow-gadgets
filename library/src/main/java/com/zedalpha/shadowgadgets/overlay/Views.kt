@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible


@SuppressLint("ViewConstructor")
internal class ViewController(override val viewGroup: ViewGroup) :
    ViewGroup(viewGroup.context), View.OnLayoutChangeListener, OverlayController<ViewShadow> {

    override val shadows = mutableListOf<ViewShadow>()

    init {
        val group = viewGroup
        if (group.isLaidOut) layout(0, 0, group.width, group.height)
        group.addOnLayoutChangeListener(this)
        group.overlay.add(this)
    }

    override fun detach() {
        super.detach()
        val group = viewGroup
        group.removeOnLayoutChangeListener(this)
        group.overlay.remove(this)
    }

    override fun createShadow(view: View) = ViewShadow(view, this)

    override fun onNewShadow(shadow: ViewShadow) {
        shadow.addShadowView(this)
    }

    override fun onRemoveShadow(shadow: ViewShadow) {
        shadow.removeShadowView(this)
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
}

@SuppressLint("ClickableViewAccessibility")
internal class ViewShadow(view: View, controller: ViewController) :
    OverlayShadow(view, controller) {

    private val shadowView = View(view.context).apply {
        background = EmptyDrawable
        outlineProvider = SurrogateViewProviderWrapper(originalProvider, view)
        clipToOutline = true
        elevation = view.elevation
        translationZ = view.translationZ
    }

    override fun updateShadow(target: View) {
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