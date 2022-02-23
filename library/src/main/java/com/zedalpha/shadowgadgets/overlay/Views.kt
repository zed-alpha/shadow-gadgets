@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible


@SuppressLint("ViewConstructor")
internal class ViewController(override val viewGroup: ViewGroup) :
    ViewGroup(viewGroup.context), OverlayController<ViewShadow>, View.OnLayoutChangeListener {

    override val shadows = mutableListOf<ViewShadow>()

    override fun createShadow(view: View) = ViewShadow(view, this)

    override fun attach() {
        super.attach()
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

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        shadows.forEach {
            if (it.willDraw) {
                it.updateShadow(false)
                val path = it.calculateClipPath()
                clipOutPath(canvas, path)
            }
        }
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    fun detachShadowView(child: View): Int {
        val index = indexOfChild(child)
        detachViewFromParent(child)
        return index
    }

    fun reAttachShadowView(child: View, index: Int) {
        attachViewToParent(child, index, null)
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

@SuppressLint("ViewConstructor")
internal class ViewShadow(
    view: View,
    private val controller: ViewController
) : OverlayShadow(view, controller) {

    private val shadowView = View(view.context).apply {
        background = EmptyDrawable
        outlineProvider = SurrogateViewProviderWrapper(originalProvider, view)
    }

    override fun onPreDraw() {
        updateShadow(true)
    }

    fun updateShadow(invalidateIfNeeded: Boolean) {
        val shadow = shadowView
        val target = targetView

        var index = 0
        if (!invalidateIfNeeded) {
            index = controller.detachShadowView(shadow)
        }

        shadow.isVisible = target.isVisible
        shadow.alpha = target.alpha
        shadow.cameraDistance = target.cameraDistance
        shadow.elevation = target.elevation
        shadow.pivotX = target.pivotX
        shadow.pivotY = target.pivotY
        shadow.layout(target.left, target.top, target.right, target.bottom)
        shadow.rotationX = target.rotationX
        shadow.rotationY = target.rotationY
        shadow.rotation = target.rotation
        shadow.scaleX = target.scaleX
        shadow.scaleY = target.scaleY
        shadow.translationX = target.translationX
        shadow.translationY = target.translationY
        shadow.translationZ = target.translationZ

        if (!invalidateIfNeeded) {
            controller.reAttachShadowView(shadowView, index)
        }
    }

    fun calculateClipPath(): Path {
        val target = targetView
        val path = CachePath
        val boundsF = CacheBoundsF

        boundsF.set(outlineBounds)
        path.rewind()
        path.addRoundRect(boundsF, outlineRadius, outlineRadius, Path.Direction.CW)

        val targetMatrix = target.matrix
        if (!targetMatrix.isIdentity) {
            path.transform(targetMatrix)
        }

        path.offset(target.left.toFloat(), target.top.toFloat())

        return path
    }

    override fun attachShadow() {
        controller.addView(shadowView)
    }

    override fun detachShadow() {
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