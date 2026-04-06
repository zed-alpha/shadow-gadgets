package com.zedalpha.shadowgadgets.view.proxy

import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.view.isVisible
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

    fun updateClip() {
        shadow = obtainShadow(shadow)
        if (shadow is ClippedShadow) target.invalidateOutline()
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

    private fun notifyModeChange(current: Plane, next: Plane) {
        val target = this.target
        val onModeChange = target.onShadowModeChange ?: return

        val nextMode = next.shadowMode
        if (current.shadowMode != nextMode) target.onModeChange(nextMode)
    }

    var layer: Layer? = null

    fun updateLayer() = plane.let { it.updateLayer(this); it.invalidate() }

    var isShown: Boolean = true

    init {
        val target = this.target
        target.shadowProxy = this

        val original = target.outlineProvider
        target.outlineProvider = ShadowOutlineProvider(original, this)
    }

    fun dispose() {
        val target = this.target
        target.shadowProxy = null

        val provider = target.outlineProvider as? ShadowOutlineProvider
        target.outlineProvider = provider?.original

        plane = Plane.Null
        shadow.dispose()

        target.onShadowModeChange?.invoke(target, ShadowMode.Native)
    }

    fun updateAndDraw(canvas: Canvas) {
        if (updateAndConfirmDraw()) shadow.draw(canvas)
    }

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

    fun invalidate() = plane.invalidate()
}

private class ShadowOutlineProvider(
    val original: ViewOutlineProvider?,
    private val proxy: ShadowProxy
) : ViewOutlineProvider() {

    override fun getOutline(view: View, outline: Outline) {
        original?.getOutline(view, outline)
        proxy.shadow.setOutline(outline)
        outline.alpha = 0F
    }
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