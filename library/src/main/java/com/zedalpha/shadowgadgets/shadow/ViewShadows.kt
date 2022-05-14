@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.ClippedShadowPlane
import com.zedalpha.shadowgadgets.ShadowFallbackStrategy.Disable
import com.zedalpha.shadowgadgets.ShadowFallbackStrategy.ForegroundOnly
import com.zedalpha.shadowgadgets.clippedShadowPlane
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.shadowFallbackStrategy


@SuppressLint("ViewConstructor")
internal class ViewShadowContainer(
    parentView: ViewGroup,
    controller: ShadowController
) : ShadowContainer<ViewShadow, ViewShadowPlane>(parentView, controller) {
    override val backgroundPlane = BackgroundViewShadowPlane()
    private val overlayProjector = OverlayProjectorView(backgroundPlane)

    override val foregroundPlane = ForegroundViewShadowPlane()

    override fun attachToParent() {
        val parent = parentView
        parent.overlay.add(overlayProjector)
        if (parent.background == null) parent.background = EmptyDrawable

        parent.overlay.add(foregroundPlane)
    }

    override fun detachFromParent() {
        val parent = parentView
        parent.overlay.remove(overlayProjector)
        if (parent.background == EmptyDrawable) parent.background = null

        parent.overlay.remove(foregroundPlane)
    }

    override fun createShadow(targetView: View, plane: ViewShadowPlane) =
        ViewShadow(targetView, controller, plane)

    override fun determinePlane(targetView: View) =
        if (!RenderNodeFactory.isOpenForBusiness &&
            targetView.shadowFallbackStrategy == ForegroundOnly
        ) {
            ClippedShadowPlane.Foreground
        } else {
            targetView.clippedShadowPlane
        }

    override fun setSize(width: Int, height: Int) {
        backgroundPlane.layout(0, 0, width, height)
        overlayProjector.layout(0, 0, width, height)

        foregroundPlane.layout(0, 0, width, height)
    }

    inner class BackgroundViewShadowPlane : ViewShadowPlane(parentView.context) {
        override fun updateAndInvalidateShadows() {
            var invalidate = false
            overlayProjector.detachProjectedView()
            shadows.forEach { if (it.update()) invalidate = true }
            overlayProjector.reattachProjectedView()
            if (invalidate) overlayProjector.invalidate()
        }
    }

    inner class ForegroundViewShadowPlane : ViewShadowPlane(parentView.context) {
        override fun updateAndInvalidateShadows() {
            var invalidate = false
            shadows.forEach { if (it.update()) invalidate = true }
            if (invalidate) parentView.invalidate()
        }
    }
}


@SuppressLint("ViewConstructor")
internal sealed class ViewShadowPlane(context: Context) :
    ViewGroup(context), Plane<ViewShadow> {

    override val shadows = mutableListOf<ViewShadow>()

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        shadows.forEach { shadow ->
            if (shadow.willDraw) {
                shadow.update()
                clipOutPath(canvas, shadow.calculateClipPath())
            }
        }
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    fun createShadowView() = ShadowView(this)

    fun addShadowView(shadowView: ShadowView) {
        addView(shadowView, EmptyLayoutParams)
    }

    fun removeShadowView(shadowView: ShadowView) {
        removeView(shadowView)
    }

    fun detachShadowView(child: ShadowView): Int {
        val index = indexOfChild(child)
        detachViewFromParent(child)
        return index
    }

    fun reattachShadowView(child: ShadowView, index: Int) {
        attachViewToParent(child, index, EmptyLayoutParams)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}


internal class ViewShadow(
    targetView: View,
    controller: ShadowController,
    viewShadowPlane: ViewShadowPlane
) : Shadow(targetView, controller, viewShadowPlane) {
    private val disabled = targetView.shadowFallbackStrategy == Disable

    private val shadowViewLazy = lazy {
        viewShadowPlane.createShadowView().apply {
            outlineProvider = SurrogateViewProviderWrapper(originalProvider, targetView)
        }
    }

    override fun attachToTarget() {
        super.attachToTarget()
        if (!disabled) shadowViewLazy.value.addToPlane()
    }

    override fun wrapOutlineProvider(targetView: View) {
        if (disabled) {
            targetView.outlineProvider = ZeroAlphaProviderWrapper(originalProvider)
        } else {
            super.wrapOutlineProvider(targetView)
        }
    }

    override fun detachFromTarget() {
        super.detachFromTarget()
        if (!disabled) shadowViewLazy.value.removeFromPlane()
    }

    override fun update(): Boolean {
        if (disabled) return false

        val target = targetView
        val shadow = shadowViewLazy.value

        val index = shadow.detachFromPlane()

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

        shadow.reattachToPlane(index)

        return colorsChanged || areaChanged
    }

    override fun show() {
        super.show()
        if (!disabled) shadowViewLazy.value.addToPlane()
    }

    override fun hide() {
        super.hide()
        if (!disabled) shadowViewLazy.value.removeFromPlane()
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
}


@SuppressLint("ViewConstructor")
internal class ShadowView(private val viewShadowPlane: ViewShadowPlane) :
    View(viewShadowPlane.context) {

    init {
        background = EmptyDrawable
    }

    fun addToPlane() {
        viewShadowPlane.addShadowView(this)
    }

    fun removeFromPlane() {
        viewShadowPlane.removeShadowView(this)
    }

    fun detachFromPlane() =
        viewShadowPlane.detachShadowView(this)

    fun reattachToPlane(index: Int) {
        viewShadowPlane.reattachShadowView(this, index)
    }

    fun changeLayout(l: Int, t: Int, r: Int, b: Int): Boolean {
        val changed = l != left || t != top || r != right || b != bottom
        if (changed) layout(l, t, r, b)
        return changed
    }

    fun changePivotX(newPivotX: Float) =
        (newPivotX != pivotX).also { if (it) pivotX = newPivotX }

    fun changePivotY(newPivotY: Float) =
        (newPivotY != pivotY).also { if (it) pivotY = newPivotY }

    fun changeScaleX(newScaleX: Float) =
        (newScaleX != scaleX).also { if (it) scaleX = newScaleX }

    fun changeScaleY(newScaleY: Float) =
        (newScaleY != scaleY).also { if (it) scaleY = newScaleY }

    fun changeTranslationX(newTranslationX: Float) =
        (newTranslationX != translationX).also { if (it) translationX = newTranslationX }

    fun changeTranslationY(newTranslationY: Float) =
        (newTranslationY != translationY).also { if (it) translationY = newTranslationY }
}


@SuppressLint("ViewConstructor")
private class OverlayProjectorView(private val projectedGroup: ViewGroup) :
    ViewGroup(projectedGroup.context) {

    init {
        projectedGroup.visibility = View.GONE
        addView(projectedGroup, EmptyLayoutParams)
        background = ViewProjectorDrawable()
    }

    fun detachProjectedView() {
        detachViewFromParent(projectedGroup)
    }

    fun reattachProjectedView() {
        attachViewToParent(projectedGroup, 0, EmptyLayoutParams)
    }

    inner class ViewProjectorDrawable : BaseDrawable() {
        override fun draw(canvas: Canvas) {
            drawChild(canvas, projectedGroup, 0L)
        }

        override fun isProjected() = true
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}


private class SurrogateViewProviderWrapper(
    private val wrapped: ViewOutlineProvider,
    private val surrogate: View
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(surrogate, outline)
    }
}

private val EmptyLayoutParams = ViewGroup.LayoutParams(0, 0)