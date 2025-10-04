package com.zedalpha.shadowgadgets.view.proxy

import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.internal.OnAttachStateChangeAdapter
import com.zedalpha.shadowgadgets.view.internal.RequiresInvalidateOnToggle
import com.zedalpha.shadowgadgets.view.internal.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.internal.isRecyclingViewGroup
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.layer.Layer
import com.zedalpha.shadowgadgets.view.pathProvider
import com.zedalpha.shadowgadgets.view.plane.Plane
import com.zedalpha.shadowgadgets.view.shadow.ClippedShadow
import com.zedalpha.shadowgadgets.view.shadow.PathProvider
import com.zedalpha.shadowgadgets.view.shadow.Shadow

internal var View.shadowProxy: ShadowProxy? by viewTag(R.id.shadow_proxy, null)
    private set

internal class ShadowProxy(val target: View) {

    var isClipped: Boolean = false
        private set

    var shadow: Shadow = obtainShadow(null)
        private set

    fun updateClip() {
        shadow = obtainShadow(shadow)
        if (isClipped) target.invalidateOutline()
        invalidate()
    }

    private fun obtainShadow(current: Shadow?): Shadow {
        val clipped = target.clipOutlineShadow
        isClipped = clipped
        return when {
            current != null ->
                when {
                    clipped == current is ClippedShadow -> current
                    current is ClippedShadow -> current.shadow
                    else -> ClippedShadow(current).also { setPathProvider(it) }
                }
            clipped -> ClippedShadow(target).also { setPathProvider(it) }
            else -> Shadow(target)
        }
    }

    fun updatePathProvider() {
        val shadow = shadow as? ClippedShadow ?: return
        setPathProvider(shadow)
        invalidate()
    }

    private fun setPathProvider(shadow: ClippedShadow) {
        val provider = target.pathProvider ?: return
        shadow.pathProvider = PathProvider { provider.getPath(target, it) }
    }

    var plane: Plane? = null
        set(next) {
            removeFromPlane(field)
            addToPlane(next)
            field = next
            invalidate()
        }

    private val recyclerParentDetach =
        object : OnAttachStateChangeAdapter {
            override fun onViewDetachedFromWindow(view: View) = dispose()
        }

    private fun addToPlane(plane: Plane?) {
        if (plane == null) return

        plane.viewGroup?.run {
            if (isRecyclingViewGroup) {
                addOnAttachStateChangeListener(recyclerParentDetach)
            }
        }
        plane.addProxy(this)
    }

    private fun removeFromPlane(plane: Plane?) {
        if (plane == null) return

        plane.viewGroup?.run {
            if (isRecyclingViewGroup) {
                removeOnAttachStateChangeListener(recyclerParentDetach)
            }
        }
        plane.removeProxy(this)
    }

    private val originalProvider: ViewOutlineProvider = target.outlineProvider

    private inner class ShadowOutlineProvider : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            originalProvider.getOutline(view, outline)
            shadow.setOutline(outline)
            outline.alpha = 0F
        }
    }

    var isShown: Boolean = true

    init {
        target.shadowProxy = this
        target.outlineProvider = ShadowOutlineProvider()
        if (RequiresInvalidateOnToggle) invalidate()
    }

    fun dispose() {
        target.shadowProxy = null
        target.outlineProvider = originalProvider
        if (RequiresInvalidateOnToggle) invalidate()

        removeFromPlane(plane)
        shadow.dispose()
    }

    fun draw(canvas: Canvas) {
        if (updateAndConfirmDraw()) shadow.draw(canvas)
    }

    fun invalidate() = plane?.invalidate()

    fun updateAndConfirmDraw(): Boolean =
        shadow.run {
            val view = target
            setPosition(view.left, view.top, view.right, view.bottom)
            alpha = view.alpha
            cameraDistance = view.cameraDistance
            elevation = view.elevation
            pivotX = view.pivotX
            pivotY = view.pivotY
            rotationX = view.rotationX
            rotationY = view.rotationY
            rotationZ = view.rotation
            scaleX = view.scaleX
            scaleY = view.scaleY
            translationX = view.translationX
            translationY = view.translationY
            translationZ = view.translationZ
            if (Build.VERSION.SDK_INT >= 28) {
                ambientColor = ViewShadowColorsHelper.getAmbientColor(view)
                spotColor = ViewShadowColorsHelper.getSpotColor(view)
            }
            isShown && view.isVisible
        }

    var layer: Layer? = null

    fun updateLayer() = plane?.updateLayer(this)
}