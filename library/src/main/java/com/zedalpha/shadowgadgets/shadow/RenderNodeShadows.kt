@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory


internal class RenderNodeShadowController(parentView: ViewGroup) :
    ShadowController<RenderNodeShadow, RenderNodeShadowContainer>(parentView) {

    override val shadowContainer = RenderNodeShadowContainer(parentView, this)

    override fun createShadowForView(view: View) = RenderNodeShadow(view, this)
}


internal class RenderNodeShadowContainer(
    parentView: ViewGroup,
    controller: RenderNodeShadowController
) : ShadowContainer<RenderNodeShadow>(parentView, controller) {

    private val foregroundDrawable = ForegroundShadowDrawable()

    private val backgroundLazy = lazy {
        val drawable = BackgroundShadowDrawable()
        parentView.overlay.add(drawable)
        if (parentView.isLaidOut) drawable.setSize(parentView.width, parentView.height)
        if (parentView.background == null) parentView.background = EmptyDrawable
        drawable
    }

    override fun attachToParent() {
        val parent = parentView
        parent.overlay.add(foregroundDrawable)
    }

    override fun detachFromParent() {
        val parent = parentView
        parent.overlay.remove(foregroundDrawable)

        if (backgroundLazy.isInitialized()) {
            parent.overlay.remove(backgroundLazy.value)
            if (parent.background == EmptyDrawable) parent.background = null
        }
    }

    override fun addShadow(shadow: RenderNodeShadow) {
        super.addShadow(shadow)
        if (shadow.isForeground) {
            foregroundDrawable.foregroundShadows += shadow
        } else {
            backgroundLazy.value.backgroundShadows += shadow
        }
    }

    override fun removeShadow(shadow: RenderNodeShadow) {
        super.removeShadow(shadow)
        if (shadow.isForeground) {
            foregroundDrawable.foregroundShadows -= shadow
        } else {
            backgroundLazy.value.backgroundShadows -= shadow
        }
    }

    override fun setSize(width: Int, height: Int) {
        if (backgroundLazy.isInitialized()) backgroundLazy.value.setSize(width, height)
    }

    private inner class ForegroundShadowDrawable : Drawable() {
        val foregroundShadows = mutableListOf<RenderNodeShadow>()

        override fun draw(canvas: Canvas) {
            foregroundShadows.forEach { it.draw(canvas) }
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun getOpacity() = PixelFormat.TRANSLUCENT
        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
    }

    private inner class BackgroundShadowDrawable : OverlayProjectorDrawable() {
        val backgroundShadows = mutableListOf<RenderNodeShadow>()

        override fun drawProjectedContent(canvas: Canvas) {
            backgroundShadows.forEach { it.draw(canvas) }
        }
    }
}


internal class RenderNodeShadow(
    targetView: View,
    shadowController: RenderNodeShadowController
) : Shadow(targetView, shadowController) {
    private val renderNode = RenderNodeFactory.newInstance()

    private var showing = true

    override fun setOutline(outline: Outline) {
        super.setOutline(outline)
        renderNode.setOutline(outline)
    }

    override fun update(): Boolean {
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

    fun draw(canvas: Canvas) {
        if (willDraw && showing) {
            update()

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

    override fun show() {
        showing = true
    }

    override fun hide() {
        showing = false
    }
}


private sealed class OverlayProjectorDrawable : Drawable() {
    private val baseNode = RenderNodeFactory.newInstance().apply {
        setClipToBounds(false)
    }

    private val projectorNode = RenderNodeFactory.newInstance().apply {
        setClipToBounds(false)
        setProjectBackwards(true)
    }

    protected abstract fun drawProjectedContent(canvas: Canvas)

    final override fun draw(canvas: Canvas) {
        val width = bounds.width()
        val height = bounds.height()
        val baseCanvas = baseNode.beginRecording(width, height)
        try {
            val projectedCanvas = projectorNode.beginRecording(width, height)
            try {
                drawProjectedContent(projectedCanvas)
            } finally {
                projectorNode.endRecording(projectedCanvas)
            }
            projectorNode.draw(baseCanvas)
        } finally {
            baseNode.endRecording(baseCanvas)
        }
        baseNode.draw(canvas)
    }

    fun setSize(width: Int, height: Int) {
        setBounds(0, 0, width, height)
        baseNode.setPosition(0, 0, width, height)
        projectorNode.setPosition(0, 0, width, height)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity() = PixelFormat.TRANSLUCENT
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(filter: ColorFilter?) {}
}

private val CacheMatrix = Matrix()