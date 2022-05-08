@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.shadow.*


internal object OverlayShadowSwitch : View.OnAttachStateChangeListener {
    override fun onViewAttachedToWindow(view: View) {
        moveShadowToOverlay(view)
    }

    override fun onViewDetachedFromWindow(view: View) {
        removeShadowFromOverlay(view)
    }
}

internal fun moveShadowToOverlay(targetView: View) {
    if (targetView.outlineProvider != null) {
        val parent = targetView.parent as? ViewGroup ?: return
        getOrCreateControllerForParent(parent).addShadow(targetView)
    }
}

internal fun removeShadowFromOverlay(targetView: View) {
    (targetView.shadow as? OverlayShadow)?.detachFromController()
}


private fun getOrCreateControllerForParent(parent: ViewGroup) =
    getControllerForParent(parent) ?: createControllerForParent(parent)

private fun getControllerForParent(parent: ViewGroup) =
    parent.getTag(R.id.tag_parent_overlay_shadow_controller) as? OverlayController<*>

private fun createControllerForParent(parent: ViewGroup) =
    createController(parent).apply { attach() }

private val createController: (ViewGroup) -> OverlayController<*> =
    if (RenderNodeFactory.isOpenForBusiness) {
        ::OverlayRenderNodeShadowController
    } else {
        ::OverlayViewShadowController
    }


private sealed interface OverlayShadow {
    val controller: OverlayController<*>

    fun detachFromController() {
        controller.removeShadow(this)
    }
}

private sealed class OverlayController<T>(protected val parentView: ViewGroup) :
    View.OnAttachStateChangeListener, ViewTreeObserver.OnDrawListener
        where T : Shadow, T : OverlayShadow {

    @CallSuper
    open fun attach() {
        val parent = parentView
        parent.setTag(R.id.tag_parent_overlay_shadow_controller, this)
        if (parent.isAttachedToWindow) addOnDrawListener()
        parent.addOnAttachStateChangeListener(this)
    }

    @CallSuper
    open fun detach() {
        val parent = parentView
        parent.setTag(R.id.tag_parent_overlay_shadow_controller, null)
        if (parent.isAttachedToWindow) removeOnDrawListener()
        parent.removeOnAttachStateChangeListener(this)
    }

    fun addShadow(view: View) {
        val shadow = createShadow(view)
        shadow.attach()
    }

    @Suppress("UNCHECKED_CAST")
    fun removeShadow(overlayShadow: OverlayShadow) {
        val shadow = overlayShadow as T
        shadow.detach()
        if (isEmpty()) detach()
    }

    override fun onViewAttachedToWindow(v: View) {
        addOnDrawListener()
    }

    override fun onViewDetachedFromWindow(v: View) {
        removeOnDrawListener()
    }

    override fun onDraw() {
        updateAndInvalidateShadows()
    }

    private fun addOnDrawListener() {
        parentView.viewTreeObserver.addOnDrawListener(this)
    }

    private fun removeOnDrawListener() {
        parentView.viewTreeObserver.removeOnDrawListener(this)
    }

    abstract fun createShadow(view: View): T
    abstract fun updateAndInvalidateShadows()
    abstract fun isEmpty(): Boolean
}


private class OverlayViewShadow(
    targetView: View,
    override val controller: OverlayViewShadowController
) : ViewShadow(targetView, controller.viewShadowContainer), OverlayShadow

private class OverlayViewShadowController(parentView: ViewGroup) :
    OverlayController<OverlayViewShadow>(parentView) {

    val viewShadowContainer = ViewShadowContainer(parentView)

    override fun attach() {
        super.attach()
        viewShadowContainer.attach()
    }

    override fun detach() {
        super.detach()
        viewShadowContainer.detach()
    }

    override fun createShadow(view: View) = OverlayViewShadow(view, this)

    override fun updateAndInvalidateShadows() {
        viewShadowContainer.updateAndInvalidateShadows()
    }

    override fun isEmpty() = viewShadowContainer.isEmpty()
}


private class OverlayRenderNodeShadow(
    targetView: View,
    override val controller: OverlayRenderNodeShadowController
) : RenderNodeShadow(targetView), OverlayShadow {
    override fun attach() {
        super.attach()
        controller.add(this)
        targetView.invalidate()
    }

    override fun detach() {
        super.detach()
        controller.remove(this)
        targetView.invalidate()
    }
}

private class OverlayRenderNodeShadowController(parentView: ViewGroup) :
    OverlayController<OverlayRenderNodeShadow>(parentView) {

    private val containerDrawable: Drawable = RenderNodeShadowContainerDrawable()

    private val shadows = mutableListOf<OverlayRenderNodeShadow>()

    override fun attach() {
        super.attach()
        parentView.overlay.add(containerDrawable)
    }

    override fun detach() {
        super.detach()
        parentView.overlay.remove(containerDrawable)
    }

    override fun createShadow(view: View) = OverlayRenderNodeShadow(view, this)

    override fun updateAndInvalidateShadows() {
        var invalidate = false
        shadows.forEach { if (it.update()) invalidate = true }
        if (invalidate) parentView.invalidate()
    }

    override fun isEmpty() = shadows.isEmpty()

    fun add(shadow: OverlayRenderNodeShadow) {
        shadows += shadow
    }

    fun remove(shadow: OverlayRenderNodeShadow) {
        shadows -= shadow
    }

    inner class RenderNodeShadowContainerDrawable : Drawable() {
        override fun draw(canvas: Canvas) {
            this@OverlayRenderNodeShadowController.shadows.forEach { it.draw(canvas) }
        }

        @Suppress("OVERRIDE_DEPRECATION")
        override fun getOpacity() = PixelFormat.TRANSLUCENT
        override fun setAlpha(alpha: Int) {}
        override fun setColorFilter(colorFilter: ColorFilter?) {}
    }
}