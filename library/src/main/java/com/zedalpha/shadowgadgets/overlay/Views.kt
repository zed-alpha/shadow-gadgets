@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.content.Context
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

    override fun createShadow(view: View) = ViewShadow(this, view)

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
        shadows.forEach { shadow ->
            if (shadow.willDraw) {
                shadow.updateShadow()
                clipOutPath(canvas, shadow.calculateClipPath())
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
    private val controller: ViewController,
    view: View
) : OverlayShadow(controller, view) {

    private val shadowView = ShadowView(view.context).apply {
        outlineProvider = SurrogateViewProviderWrapper(originalProvider, view)
    }

    override fun onPreDraw() {
        if (updateShadow()) shadowView.invalidate()
    }

    fun updateShadow(): Boolean {
        val shadow = shadowView
        val target = targetView

        val index = controller.detachShadowView(shadow)

        shadow.isVisible = target.isVisible
        shadow.alpha = target.alpha
        shadow.cameraDistance = target.cameraDistance
        shadow.elevation = target.elevation
        shadow.rotationX = target.rotationX
        shadow.rotationY = target.rotationY
        shadow.rotation = target.rotation
        shadow.translationZ = target.translationZ

        val colorsChanged = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ShadowColorsHelper.changeColors(shadow, target)
        } else {
            false
        }

        val areaChanged =
            shadow.changeLayout(target.left, target.top, target.right, target.bottom) or
                    shadow.changePivotX(target.pivotX) or
                    shadow.changePivotY(target.pivotY) or
                    shadow.changeScaleX(target.scaleX) or
                    shadow.changeScaleY(target.scaleY) or
                    shadow.changeTranslationX(target.translationX) or
                    shadow.changeTranslationY(target.translationY)

        controller.reAttachShadowView(shadow, index)

        return colorsChanged || areaChanged
    }

    fun calculateClipPath(): Path {
        val path = CachePath
        path.set(clipPath)

        val target = targetView
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

private class ShadowView(context: Context) : View(context) {
    init {
        background = EmptyDrawable
    }

    fun changeLayout(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = l != left || t != top || r != right || b != bottom
        if (changed) layout(l, t, r, b)
        return changed
    }

    fun changePivotX(newPivotX: Float) = (newPivotX != pivotX).also { if (it) pivotX = newPivotX }

    fun changePivotY(newPivotY: Float) = (newPivotY != pivotY).also { if (it) pivotY = newPivotY }

    fun changeScaleX(newScaleX: Float) = (newScaleX != scaleX).also { if (it) scaleX = newScaleX }

    fun changeScaleY(newScaleY: Float) = (newScaleY != scaleY).also { if (it) scaleY = newScaleY }

    fun changeTranslationX(newTranslationX: Float) =
        (newTranslationX != translationX).also { if (it) translationX = newTranslationX }

    fun changeTranslationY(newTranslationY: Float) =
        (newTranslationY != translationY).also { if (it) translationY = newTranslationY }
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