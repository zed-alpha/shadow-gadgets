@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory


internal sealed class Shadow(val targetView: View) {
    protected val originalProvider: ViewOutlineProvider = targetView.outlineProvider
    protected val clipPath = Path()

    val willDraw: Boolean
        get() = targetView.isVisible && !clipPath.isEmpty

    fun attachToTargetView() {
        targetView.outlineProvider = ProviderWrapper(originalProvider, this)
    }

    fun detachFromTargetView() {
        targetView.outlineProvider = originalProvider
    }

    @Suppress("LiftReturnOrAssignment")
    open fun setOutline(outline: Outline) {
        val path = clipPath
        path.reset()

        if (outline.isEmpty) return

        val outlineBounds = CacheRect
        if (getOutlineRect(outline, outlineBounds)) {
            val boundsF = CacheRectF
            boundsF.set(outlineBounds)
            val outlineRadius = getOutlineRadius(outline)
            path.addRoundRect(boundsF, outlineRadius, outlineRadius, Path.Direction.CW)
        } else {
            setOutlinePath(outline, path)
        }
    }

    abstract fun updateShadow(): Boolean
}

internal open class RenderNodeShadow(targetView: View) : Shadow(targetView) {
    private val renderNode = RenderNodeFactory.newInstance()

    override fun setOutline(outline: Outline) {
        super.setOutline(outline)
        renderNode.setOutline(outline)
    }

    fun draw(canvas: Canvas) {
        if (willDraw) {
            updateShadow()

            val path = CachePath
            path.set(clipPath)

            val node = renderNode
            if (!node.hasIdentityMatrix()) {
                val matrix = CacheMatrix
                node.getMatrix(matrix)
                path.transform(matrix)
            }

            val target = targetView
            path.offset(target.left.toFloat(), target.top.toFloat())
            clipAndDraw(canvas, path, renderNode)
        }
    }

    override fun updateShadow(): Boolean {
        val node = renderNode
        val target = targetView

        val left = target.left
        val top = target.top
        val right = target.right
        val bottom = target.bottom

        node.setAlpha(target.alpha)
        node.setCameraDistance(target.cameraDistance)
        node.setElevation(target.elevation)
        node.setRotationX(target.rotationX)
        node.setRotationY(target.rotationY)
        node.setRotationZ(target.rotation)
        node.setTranslationZ(target.translationZ)

        val colorsChanged = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ShadowColorsHelper.changeColors(node, target)
        } else {
            false
        }

        val areaChanged = node.setPivotX(target.pivotX) or
                node.setPivotY(target.pivotY) or
                node.setPosition(left, top, right, bottom) or
                node.setScaleX(target.scaleX) or
                node.setScaleY(target.scaleY) or
                node.setTranslationX(target.translationX) or
                node.setTranslationY(target.translationY)

        return colorsChanged || areaChanged
    }
}

internal open class ViewShadow(targetView: View) : Shadow(targetView) {
    val shadowView = ShadowView(targetView.context).apply {
        outlineProvider = SurrogateViewProviderWrapper(originalProvider, targetView)
    }

    override fun updateShadow(): Boolean {
        val shadow = shadowView
        val target = targetView

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

    class ShadowView(context: Context) : View(context) {
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
}

private class ProviderWrapper(
    private val wrapped: ViewOutlineProvider,
    private val shadow: Shadow
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        shadow.setOutline(outline)
        outline.alpha = 0.0F
    }
}

private object EmptyDrawable : Drawable() {
    override fun draw(canvas: Canvas) {}
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(filter: ColorFilter?) {}
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

private class SurrogateViewProviderWrapper(
    private val wrapped: ViewOutlineProvider,
    private val surrogate: View
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(surrogate, outline)
    }
}

private val CacheRect = Rect()
private val CacheRectF = RectF()
private val CachePath = Path()
private val CacheMatrix = Matrix()