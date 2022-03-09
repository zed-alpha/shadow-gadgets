@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory


internal object ShadowSwitch : View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(view: View) {
        moveShadowToOverlay(view)
    }

    override fun onViewDetachedFromWindow(view: View) {
        removeShadowFromOverlay(view)
    }
}

internal fun moveShadowToOverlay(targetView: View) {
    if (targetView.isHardwareAccelerated && targetView.outlineProvider != null) {
        val parent = targetView.parent as? ViewGroup ?: return
        getOrCreateControllerForParent(parent).addShadow(targetView)
    }
}

internal fun removeShadowFromOverlay(targetView: View) {
    (targetView.getTag(R.id.tag_target_overlay_shadow) as? OverlayShadow)?.detachFromController()
}


private fun getOrCreateControllerForParent(parent: ViewGroup) =
    getControllerForParent(parent) ?: createControllerForParent(parent)

private fun getControllerForParent(parent: ViewGroup) =
    parent.getTag(R.id.tag_parent_overlay_shadow_controller) as? OverlayController<*>

private fun createControllerForParent(parent: ViewGroup): OverlayController<*> {
    val controller =
        if (RenderNodeFactory.isOpenForBusiness) {
            RenderNodeOverlayController(parent)
        } else {
            ViewOverlayController(parent)
        }
    controller.attach()
    return controller
}

internal sealed interface OverlayShadow {
    val controller: OverlayController<*>

    fun detachFromController() {
        controller.removeShadow(this)
    }

    fun attachShadowToOverlay()
    fun detachShadowFromOverlay()
    fun onPreDraw()
}

internal sealed class OverlayController<T>(protected val parentView: ViewGroup) :
    ViewTreeObserver.OnPreDrawListener where T : Shadow, T : OverlayShadow {

    abstract val shadows: MutableList<T>

    open fun attach() {
        val parent = parentView
        parent.setTag(R.id.tag_parent_overlay_shadow_controller, this)
        parent.viewTreeObserver.addOnPreDrawListener(this)
    }

    protected open fun detach() {
        val parent = parentView
        parent.setTag(R.id.tag_parent_overlay_shadow_controller, null)
        parent.viewTreeObserver.removeOnPreDrawListener(this)
    }

    abstract fun createShadow(view: View): T

    fun addShadow(view: View) {
        val shadow = createShadow(view)
        shadow.targetView.setTag(R.id.tag_target_overlay_shadow, shadow)
        shadow.attachToTargetView()
        shadow.attachShadowToOverlay()
        shadows += shadow
    }

    @Suppress("UNCHECKED_CAST")
    fun removeShadow(overlayShadow: OverlayShadow) {
        val shadow = overlayShadow as T
        shadow.targetView.setTag(R.id.tag_target_overlay_shadow, null)
        shadow.detachFromTargetView()
        shadow.detachShadowFromOverlay()
        shadows -= shadow
        if (shadows.isEmpty()) detach()
    }

    override fun onPreDraw(): Boolean {
        shadows.forEach { it.onPreDraw() }
        return true
    }
}

internal class RenderNodeOverlayShadow(
    targetView: View,
    override val controller: RenderNodeOverlayController
) : RenderNodeShadow(targetView), OverlayShadow {

    private val drawable = RenderNodeDrawable()

    override fun attachShadowToOverlay() {
        controller.addShadowDrawable(drawable)
        targetView.invalidate()
    }

    override fun detachShadowFromOverlay() {
        controller.removeShadowDrawable(drawable)
        targetView.invalidate()
    }

    override fun onPreDraw() {
        if (updateShadow()) drawable.invalidateSelf()
    }

    inner class RenderNodeDrawable : Drawable() {
        override fun draw(canvas: Canvas) {
            this@RenderNodeOverlayShadow.draw(canvas)
        }

        override fun setAlpha(alpha: Int) {}

        override fun setColorFilter(colorFilter: ColorFilter?) {}

        override fun getOpacity() = PixelFormat.TRANSLUCENT
    }
}

internal class RenderNodeOverlayController(parentView: ViewGroup) :
    OverlayController<RenderNodeOverlayShadow>(parentView) {

    override val shadows = mutableListOf<RenderNodeOverlayShadow>()

    override fun createShadow(view: View) = RenderNodeOverlayShadow(view, this)

    fun addShadowDrawable(drawable: Drawable) {
        parentView.overlay.add(drawable)
    }

    fun removeShadowDrawable(drawable: Drawable) {
        parentView.overlay.remove(drawable)
    }
}

internal class ViewOverlayShadow(
    targetView: View,
    override val controller: ViewOverlayController
) : ViewShadow(targetView), OverlayShadow {

    override fun attachShadowToOverlay() {
        controller.addShadowView(shadowView)
    }

    override fun detachShadowFromOverlay() {
        controller.removeShadowView(shadowView)
    }

    override fun onPreDraw() {
        if (updateShadow()) shadowView.invalidate()
    }

    override fun updateShadow(): Boolean {
        val view = shadowView
        val index = controller.detachShadowView(view)
        val result = super.updateShadow()
        controller.reAttachShadowView(view, index)
        return result
    }
}

internal class ViewOverlayController(parentView: ViewGroup) :
    OverlayController<ViewOverlayShadow>(parentView) {

    override val shadows = mutableListOf<ViewOverlayShadow>()

    override fun createShadow(view: View) = ViewOverlayShadow(view, this)

    private val container = ViewShadowContainer(parentView.context, shadows)

    override fun attach() {
        super.attach()
        val parent = parentView
        val container = container
        if (parent.isLaidOut) container.layout(0, 0, parent.width, parent.height)
        parent.addOnLayoutChangeListener(container)
        parent.overlay.add(container)
    }

    override fun detach() {
        super.detach()
        val parent = parentView
        val container = container
        parent.removeOnLayoutChangeListener(container)
        parent.overlay.remove(container)
    }

    fun detachShadowView(shadowView: ViewShadow.ShadowView) = container.detachShadowView(shadowView)

    fun reAttachShadowView(shadowView: ViewShadow.ShadowView, index: Int) {
        container.reAttachShadowView(shadowView, index)
    }

    fun addShadowView(shadowView: ViewShadow.ShadowView) {
        container.addView(shadowView)
    }

    fun removeShadowView(shadowView: ViewShadow.ShadowView) {
        container.removeView(shadowView)
    }
}