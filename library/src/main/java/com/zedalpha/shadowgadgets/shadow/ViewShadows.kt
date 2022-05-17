@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import android.view.ViewOutlineProvider
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.ClippedShadowPlane
import com.zedalpha.shadowgadgets.ShadowFallbackStrategy.ChangePlane
import com.zedalpha.shadowgadgets.ShadowFallbackStrategy.DisableShadow
import com.zedalpha.shadowgadgets.clippedShadowPlane
import com.zedalpha.shadowgadgets.shadowFallbackStrategy


@SuppressLint("ViewConstructor")
internal class ViewShadowContainer(
    parentView: ViewGroup,
    controller: ShadowController
) : ShadowContainer<ViewShadow, ViewShadowPlane>(parentView, controller) {
    override val foregroundPlane = ForegroundViewShadowPlane(parentView)

    override val backgroundPlane = BackgroundViewShadowPlane(parentView)

    override fun createShadow(targetView: View, plane: ViewShadowPlane) =
        ViewShadow(targetView, controller, plane)

    override fun determinePlane(targetView: View): ClippedShadowPlane =
        if (targetView.shadowFallbackStrategy == ChangePlane) {
            targetView.clippedShadowPlane.otherPlane
        } else {
            targetView.clippedShadowPlane
        }
}


internal class ForegroundViewShadowPlane(parentView: ViewGroup) : ViewShadowPlane(parentView) {
    override fun updateShadowsAndInvalidate() {
        var invalidate = false
        shadows.forEach { if (it.update()) invalidate = true }
        if (invalidate) parentView.invalidate()
    }
}

internal class BackgroundViewShadowPlane(parentView: ViewGroup) : ViewShadowPlane(parentView) {
    private val overlayProjector = OverlayProjectorView(viewShadowGroup)

    override fun addToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(overlayProjector)
        checkAddProjectionReceiver()
    }

    override fun removeFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(overlayProjector)
        checkRemoveProjectionReceiver()
    }

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        overlayProjector.layout(0, 0, width, height)
    }

    override fun updateShadowsAndInvalidate() {
        var invalidate = false
        overlayProjector.detachProjectedView()
        shadows.forEach { if (it.update()) invalidate = true }
        overlayProjector.reattachProjectedView()
        if (invalidate) overlayProjector.invalidate()
    }
}

internal sealed class ViewShadowPlane(parentView: ViewGroup) :
    ContainerPlane<ViewShadow>(parentView) {

    protected val viewShadowGroup = ViewShadowGroup()

    override fun addToOverlay(overlay: ViewGroupOverlay) {
        overlay.add(viewShadowGroup)
    }

    override fun removeFromOverlay(overlay: ViewGroupOverlay) {
        overlay.remove(viewShadowGroup)
    }

    @CallSuper
    override fun setSize(width: Int, height: Int) {
        viewShadowGroup.layout(0, 0, width, height)
    }

    fun createShadowView() = ShadowView()

    inner class ViewShadowGroup : ViewGroup(parentView.context) {
        fun detachShadowView(child: ShadowView): Int {
            val index = indexOfChild(child)
            detachViewFromParent(child)
            return index
        }

        fun reattachShadowView(child: ShadowView, index: Int) {
            attachViewToParent(child, index, EmptyLayoutParams)
        }

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

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
    }

    @SuppressLint("ViewConstructor")
    inner class ShadowView : View(parentView.context) {
        init {
            background = EmptyDrawable
        }

        fun addToGroup() {
            viewShadowGroup.addView(this, EmptyLayoutParams)
        }

        fun removeFromGroup() {
            viewShadowGroup.removeView(this)
        }

        fun detachFromGroup() =
            viewShadowGroup.detachShadowView(this)

        fun reattachToGroup(index: Int) {
            viewShadowGroup.reattachShadowView(this, index)
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
}


internal class ViewShadow(
    targetView: View,
    controller: ShadowController,
    viewShadowPlane: ViewShadowPlane
) : Shadow(targetView, controller, viewShadowPlane) {
    private val disabled = targetView.shadowFallbackStrategy == DisableShadow

    private val shadowView by lazy {
        viewShadowPlane.createShadowView().apply {
            outlineProvider = SurrogateViewProviderWrapper(originalProvider, targetView)
        }
    }

    override fun attachToTarget() {
        super.attachToTarget()
        if (!disabled) shadowView.addToGroup()
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
        if (!disabled) shadowView.removeFromGroup()
    }

    override fun update(): Boolean {
        if (disabled) return false

        val target = targetView
        val shadow = shadowView

        val index = shadow.detachFromGroup()

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

        shadow.reattachToGroup(index)

        return colorsChanged || areaChanged
    }

    override fun show() {
        super.show()
        if (!disabled) shadowView.addToGroup()
    }

    override fun hide() {
        super.hide()
        if (!disabled) shadowView.removeFromGroup()
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