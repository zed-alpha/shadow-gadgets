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
import com.zedalpha.shadowgadgets.disableShadowOnFallback
import com.zedalpha.shadowgadgets.isDisableShadowOnFallbackExplicitlySet
import com.zedalpha.shadowgadgets.viewgroup.ClippedShadowsViewGroup
import com.zedalpha.shadowgadgets.viewgroup.clippedShadowsLayoutParams


internal class ViewShadowController(parentView: ViewGroup) :
    ShadowController<ViewShadow, ViewShadowContainer>(parentView) {

    override val shadowContainer = ViewShadowContainer(parentView, this)

    override fun createShadowForView(view: View) = ViewShadow(view, this)
}


@SuppressLint("ViewConstructor")
internal class ViewShadowContainer(
    parentView: ViewGroup,
    controller: ViewShadowController
) : ShadowContainer<ViewShadow>(parentView, controller) {

    private val shadowViewGroup = ShadowViewGroup()

    override fun attachToParent() {
        parentView.overlay.add(shadowViewGroup)
    }

    override fun detachFromParent() {
        parentView.overlay.remove(shadowViewGroup)
    }

    override fun setSize(width: Int, height: Int) {
        shadowViewGroup.layout(0, 0, width, height)
    }

    fun addShadowView(shadowView: ShadowView) {
        shadowViewGroup.addView(shadowView, EmptyLayoutParams)
    }

    fun removeShadowView(shadowView: ShadowView) {
        shadowViewGroup.removeView(shadowView)
    }

    fun detachShadowView(child: ShadowView) =
        shadowViewGroup.detachShadowView(child)

    fun reattachShadowView(child: ShadowView, index: Int) {
        shadowViewGroup.reattachShadowView(child, index)
    }

    inner class ShadowViewGroup : ViewGroup(parentView.context) {
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

        fun detachShadowView(child: ShadowView): Int {
            val index = indexOfChild(child)
            detachViewFromParent(child)
            return index
        }

        fun reattachShadowView(child: ShadowView, index: Int) {
            attachViewToParent(child, index, EmptyLayoutParams)
        }

        override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
            /* No-op. Child layout is handled manually elsewhere. */
        }
    }
}


internal class ViewShadow(
    targetView: View,
    shadowController: ViewShadowController
) : Shadow(targetView, shadowController) {

    private val disabled: Boolean

    init {
        val shadowsParent = targetView.parent as? ClippedShadowsViewGroup
        disabled =
            if (shadowsParent != null && !targetView.isDisableShadowOnFallbackExplicitlySet) {
                targetView.clippedShadowsLayoutParams?.disableShadowOnFallback
                    ?: shadowsParent.disableChildShadowsOnFallback
            } else {
                targetView.disableShadowOnFallback
            }
    }

    private val container = shadowController.shadowContainer

    private val shadowView = lazy {
        ShadowView(targetView.context).apply {
            outlineProvider = SurrogateViewProviderWrapper(originalProvider, targetView)
        }
    }

    override fun attachToTarget() {
        super.attachToTarget()
        if (!disabled) container.addShadowView(shadowView.value)
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
        if (!disabled) container.removeShadowView(shadowView.value)
    }

    override fun update(): Boolean {
        if (disabled) return false

        val target = targetView
        val shadow = shadowView.value

        val index = container.detachShadowView(shadow)

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

        container.reattachShadowView(shadow, index)

        return colorsChanged || areaChanged
    }

    override fun show() {
        if (!disabled) container.addShadowView(shadowView.value)
    }

    override fun hide() {
        if (!disabled) container.removeShadowView(shadowView.value)
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


internal class ShadowView(context: Context) : View(context) {
    init {
        background = EmptyDrawable
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

private class SurrogateViewProviderWrapper(
    private val wrapped: ViewOutlineProvider,
    private val surrogate: View
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(surrogate, outline)
    }
}

private val EmptyLayoutParams = ViewGroup.LayoutParams(0, 0)