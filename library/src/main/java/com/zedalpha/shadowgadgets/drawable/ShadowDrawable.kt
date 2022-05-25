@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.drawable

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.IntRange
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.rendernode.RenderNodeColors
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper
import com.zedalpha.shadowgadgets.shadow.*

class ShadowDrawable private constructor() : Drawable() {
    companion object {
        val isAvailable = if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            IsPieReflectionAvailable
        } else {
            RenderNodeFactory.isOpenForBusiness
        }

        fun fromView(view: View): ShadowDrawable {
            checkAvailability()

            val drawable = ShadowDrawable()
            drawable.updateFromView(view)
            return drawable
        }

        fun fromPath(path: Path): ShadowDrawable {
            checkAvailability()

            val drawable = ShadowDrawable()
            drawable.updateFromPath(path)
            return drawable
        }

        private fun checkAvailability() {
            if (!isAvailable) throw IllegalStateException("ShadowDrawable is unavailable.")
        }
    }

    private val renderNode: RenderNodeWrapper = createRenderNodeAndTryExtraHard()

    private val clipPath = Path()
    private var renderNodeRight = 0
    private var renderNodeBottom = 0

    private var defaultWidth = -1
    private var defaultHeight = -1

    var fillPaint: Paint? = null

    override fun getIntrinsicWidth() = defaultWidth

    override fun getIntrinsicHeight() = defaultHeight

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun draw(canvas: Canvas) {
        if (!canvas.isHardwareAccelerated) return

        val path = cachePath
        path.set(clipPath)

        val node = renderNode
        if (!node.hasIdentityMatrix()) {
            val matrix = cacheMatrix
            node.getMatrix(matrix)
            path.transform(matrix)
        }

        val left = bounds.exactCenterX() - renderNodeRight / 2
        val top = bounds.exactCenterY() - renderNodeBottom / 2
        canvas.translate(left, top)
        clipAndDraw(canvas, path, node)
        fillPaint?.let { canvas.drawPath(path, it) }
        canvas.translate(-left, -top)
    }

    override fun setAlpha(@IntRange(from = 0, to = 255) alpha: Int) {
        if (renderNode.setAlpha(alpha / 255F)) {
            fillPaint?.alpha = alpha
            invalidateSelf()
        }
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        /* No-op, for now */
    }

    private fun setRenderNodePosition(right: Int, bottom: Int): Boolean {
        renderNodeRight = right
        renderNodeBottom = bottom
        return renderNode.setPosition(0, 0, right, bottom)
    }

    var elevation: Float
        get() = renderNode.getElevation()
        set(value) {
            if (renderNode.setElevation(value)) invalidateSelf()
        }

    var pivotX: Float
        get() = renderNode.getPivotX()
        set(value) {
            if (renderNode.setPivotX(value)) invalidateSelf()
        }

    var pivotY: Float
        get() = renderNode.getPivotY()
        set(value) {
            if (renderNode.setPivotY(value)) invalidateSelf()
        }

    var rotationX: Float
        get() = renderNode.getRotationX()
        set(value) {
            if (renderNode.setRotationX(value)) invalidateSelf()
        }

    var rotationY: Float
        get() = renderNode.getRotationY()
        set(value) {
            if (renderNode.setRotationY(value)) invalidateSelf()
        }

    var rotationZ: Float
        get() = renderNode.getRotationZ()
        set(value) {
            if (renderNode.setRotationZ(value)) invalidateSelf()
        }

    var scaleX: Float
        get() = renderNode.getScaleX()
        set(value) {
            if (renderNode.setScaleX(value)) invalidateSelf()
        }

    var scaleY: Float
        get() = renderNode.getScaleY()
        set(value) {
            if (renderNode.setScaleY(value)) invalidateSelf()
        }

    var translationX: Float
        get() = renderNode.getTranslationX()
        set(value) {
            if (renderNode.setTranslationX(value)) invalidateSelf()
        }

    var translationY: Float
        get() = renderNode.getTranslationY()
        set(value) {
            if (renderNode.setTranslationY(value)) invalidateSelf()
        }

    var translationZ: Float
        get() = renderNode.getTranslationZ()
        set(value) {
            if (renderNode.setTranslationZ(value)) invalidateSelf()
        }

    @get:RequiresApi(Build.VERSION_CODES.P)
    @set:RequiresApi(Build.VERSION_CODES.P)
    var ambientShadowColor: Int
        get() = (renderNode as RenderNodeColors).getAmbientShadowColor()
        set(value) {
            if ((renderNode as RenderNodeColors).setAmbientShadowColor(value)) invalidateSelf()
        }

    @get:RequiresApi(Build.VERSION_CODES.P)
    @set:RequiresApi(Build.VERSION_CODES.P)
    var spotShadowColor: Int
        get() = (renderNode as RenderNodeColors).getSpotShadowColor()
        set(value) {
            if ((renderNode as RenderNodeColors).setSpotShadowColor(value)) invalidateSelf()
        }

    fun updateFromView(target: View) {
        val node = renderNode
        val originalProvider = target.outlineProvider
        target.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                originalProvider.getOutline(target, outline)
                node.setOutline(outline)
                clipPath.set(extractPathFromOutline(outline))
            }
        }
        target.invalidateOutline()
        target.outlineProvider = originalProvider

        val areaChanged = node.setAlpha(target.alpha) or
                node.setCameraDistance(target.cameraDistance) or
                node.setElevation(target.elevation) or
                node.setPivotX(target.pivotX) or
                node.setPivotY(target.pivotY) or
                setRenderNodePosition(target.width, target.height) or
                node.setRotationX(target.rotationX) or
                node.setRotationY(target.rotationY) or
                node.setRotationZ(target.rotation) or
                node.setScaleX(target.scaleX) or
                node.setScaleY(target.scaleY) or
                node.setTranslationX(target.translationX) or
                node.setTranslationY(target.translationY) or
                node.setTranslationZ(target.translationZ)

        defaultWidth = target.width
        defaultHeight = target.height

        val colorsChanged = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ShadowColorsHelper.changeColors(node, target)
        } else {
            false
        }

        if (areaChanged || colorsChanged) invalidateSelf()
    }

    fun updateFromPath(path: Path) {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && !path.isConvex) {
            throw IllegalArgumentException("Path must be convex on API level 28 and below.")
        }

        val outlineBoundsF = cacheRectF
        if (path.isEmpty) {
            outlineBoundsF.setEmpty()
        } else {
            path.computeBounds(outlineBoundsF, true)
        }

        val node = renderNode
        if (outlineBoundsF.isEmpty) {
            node.setOutline(null)
            setRenderNodePosition(0, 0)
        } else {
            val outline = cacheOutline
            outline.setEmpty()
            outline.alpha = 1.0F
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                outline.setPath(path)
            } else {
                try {
                    @Suppress("DEPRECATION")
                    outline.setConvexPath(path)
                } catch (ignored: IllegalArgumentException) {
                    // setConvexPath() is misnamed in Q; it accepts any kind. However,
                    // early releases of Q weren't ready for concave ones yet, apparently.
                    // We mimic MaterialShapeDrawable's behavior here and ignore it.
                }
            }
            node.setOutline(outline)

            clipPath.set(path)
            val width = outlineBoundsF.right.toInt()
            val height = outlineBoundsF.bottom.toInt()
            defaultWidth = width
            defaultHeight = height
            setRenderNodePosition(width, height)
        }
        invalidateSelf()
    }

    private fun extractPathFromOutline(outline: Outline): Path {
        val path = cachePath
        val outlineBounds = cacheRect
        if (getOutlineRect(outline, outlineBounds)) {
            val outlineBoundsF = cacheRectF
            outlineBoundsF.set(outlineBounds)
            val outlineRadius = getOutlineRadius(outline)
            path.addRoundRect(outlineBoundsF, outlineRadius, outlineRadius, Path.Direction.CW)
        } else {
            setOutlinePath(outline, path)
        }
        return path
    }

    private val cacheOutline = Outline()
    private val cachePath = Path()
    private val cacheRectF = RectF()
    private val cacheRect = Rect()
    private val cacheMatrix = Matrix()
}

private val createRenderNodeAndTryExtraHard: () -> RenderNodeWrapper =
    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
        ::PieReflectorWrapper
    } else {
        RenderNodeFactory::newInstance
    }