@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory

internal abstract class RenderNodeShadow(targetView: View) : Shadow(targetView) {
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

    override fun show() {
        showing = true
    }

    override fun hide() {
        showing = false
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
}