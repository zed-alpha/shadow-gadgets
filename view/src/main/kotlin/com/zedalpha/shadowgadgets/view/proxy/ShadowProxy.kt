package com.zedalpha.shadowgadgets.view.proxy

import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.view.ExperimentalShadowGadgets
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowMode
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.internal.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.layer.Layer
import com.zedalpha.shadowgadgets.view.onShadowModeChange
import com.zedalpha.shadowgadgets.view.pathProvider
import com.zedalpha.shadowgadgets.view.plane.Plane
import com.zedalpha.shadowgadgets.view.shadow.ClippedShadow
import com.zedalpha.shadowgadgets.view.shadow.PathProvider
import com.zedalpha.shadowgadgets.view.shadow.Shadow
import com.zedalpha.shadowgadgets.view.shadowMode

internal var View.shadowProxy: ShadowProxy? by viewTag(R.id.shadow_proxy, null)
    private set

internal class ShadowProxy(val target: View) {

    var shadow: Shadow = obtainShadow(null)
        private set

    val isClipped: Boolean get() = shadow is ClippedShadow

    fun updateClip() {
        shadow = obtainShadow(shadow)
        if (isClipped) target.invalidateOutline()
        plane.invalidate()
    }

    private fun obtainShadow(current: Shadow?): Shadow {
        val clipped = target.clipOutlineShadow
        return if (current != null) {
            if (current is ClippedShadow == clipped) current
            else if (current is ClippedShadow) current.shadow
            else ClippedShadow(current).also { setPathProvider(it) }
        } else {
            if (clipped) ClippedShadow(target).also { setPathProvider(it) }
            else Shadow(target)
        }
    }

    fun updatePathProvider() {
        val shadow = shadow as? ClippedShadow ?: return
        setPathProvider(shadow)
        plane.invalidate()
    }

    private fun setPathProvider(shadow: ClippedShadow) {
        val provider = target.pathProvider ?: return
        shadow.pathProvider = PathProvider { provider.getPath(target, it) }
    }

    private var recyclingParent: RecyclingParent? = null

    val isChildOfRecyclingViewGroup: Boolean get() = recyclingParent != null

    fun requireParentRecyclingViewGroup(): ViewGroup =
        checkNotNull(recyclingParent) { "Missing RecyclingParent" }.viewGroup

    var plane: Plane = Plane.Initial
        set(next) {
            if (field === next) return

            if (field.viewGroup !== next.viewGroup) {
                recyclingParent?.dispose()
                recyclingParent =
                    next.viewGroup
                        ?.takeIf { it.isRecycling }
                        ?.let { RecyclingParent(it, this) }
                isShown = true
            }

            field.removeProxy(this)
            field.invalidate()

            next.addProxy(this)
            next.invalidate()

            notifyModeChange(field, next)

            field = next
        }

    @OptIn(ExperimentalShadowGadgets::class)
    private fun notifyModeChange(current: Plane, next: Plane) {
        val target = this.target
        val onModeChange = target.onShadowModeChange ?: return

        val nextMode = next.shadowMode
        if (current.shadowMode != nextMode) target.onModeChange(nextMode)
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
    }

    fun dispose() {
        target.shadowProxy = null
        target.outlineProvider = originalProvider

        plane = Plane.Null
        shadow.dispose()

        @OptIn(ExperimentalShadowGadgets::class)
        target.onShadowModeChange?.invoke(target, ShadowMode.Native)
    }

    fun updateAndDraw(canvas: Canvas) {
        if (updateAndConfirmDraw()) shadow.draw(canvas)
    }

    fun invalidate() = plane.invalidate()

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
            isShown && view.isVisible && view.z > 0F
        }

    var layer: Layer? = null

    fun updateLayer() = plane.let { it.updateLayer(this); it.invalidate() }
}

private class RecyclingParent(
    val viewGroup: ViewGroup,
    private val proxy: ShadowProxy
) {
    private val disposeOnDetach =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {}
            override fun onViewDetachedFromWindow(view: View) = proxy.dispose()
        }

    init {
        viewGroup.addOnAttachStateChangeListener(disposeOnDetach)
    }

    fun dispose() = viewGroup.removeOnAttachStateChangeListener(disposeOnDetach)
}