@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.view.*
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.ClippedShadowPlane
import com.zedalpha.shadowgadgets.ClippedShadowPlane.Foreground
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.shouldUseFallbackMethod


internal class ShadowController(private val parentView: ViewGroup) :
    View.OnAttachStateChangeListener, ViewTreeObserver.OnDrawListener {

    val isUsingFallbackMethod = parentView.shouldUseFallbackMethod

    private val shadowContainer = if (isUsingFallbackMethod) {
        ViewShadowContainer(parentView, this)
    } else {
        RenderNodeShadowContainer(parentView, this)
    }

    fun attachToParent() {
        val parent = parentView
        parent.shadowController = this
        if (parent.isAttachedToWindow) addDrawListener()
        parent.addOnAttachStateChangeListener(this)
    }

    fun detachFromParent() {
        val parent = parentView
        parent.shadowController = null
        if (parent.isAttachedToWindow) removeDrawListener()
        parent.removeOnAttachStateChangeListener(this)
    }

    fun addShadowForView(targetView: View) {
        shadowContainer.addShadowForView(targetView)
    }

    fun removeShadow(shadow: Shadow) {
        shadowContainer.removeShadow(shadow)
    }

    fun notifyAttributeChanged(shadow: Shadow, targetView: View) {
        removeShadow(shadow)
        // Current instance might've detached in removeShadow; must getOrCreate.
        getOrCreateController(parentView).addShadowForView(targetView)
    }

    override fun onViewAttachedToWindow(v: View) {
        addDrawListener()
    }

    override fun onViewDetachedFromWindow(v: View) {
        removeDrawListener()
    }

    override fun onDraw() {
        shadowContainer.updateShadowsAndInvalidate()
    }

    private fun addDrawListener() {
        parentView.viewTreeObserver.addOnDrawListener(this)
    }

    private fun removeDrawListener() {
        parentView.viewTreeObserver.removeOnDrawListener(this)
    }
}


internal sealed class ShadowContainer<S : Shadow, P : ContainerPlane<S>>(
    private val parentView: ViewGroup,
    protected val controller: ShadowController
) {
    abstract val foregroundPlane: P
    abstract val backgroundPlane: P

    fun addShadowForView(targetView: View) {
        val plane = if (determinePlane(targetView) == Foreground) {
            foregroundPlane
        } else {
            backgroundPlane
        }
        createShadow(targetView, plane).attachToTarget()
    }

    fun removeShadow(shadow: Shadow) {
        shadow.detachFromTarget()
        if (foregroundPlane.isEmpty() && backgroundPlane.isEmpty()) {
            controller.detachFromParent()
        }
    }

    fun updateShadowsAndInvalidate() {
        backgroundPlane.updateShadowsAndInvalidate()
        foregroundPlane.updateShadowsAndInvalidate()
    }

    abstract fun createShadow(targetView: View, plane: P): S
    abstract fun determinePlane(targetView: View): ClippedShadowPlane
}


internal sealed class ContainerPlane<S : Shadow>(protected val parentView: ViewGroup) :
    View.OnLayoutChangeListener {

    protected val shadows = mutableListOf<S>()

    fun isEmpty() = shadows.isEmpty()

    fun addShadow(shadow: Shadow) {
        if (shadows.isEmpty()) attachToParent()
        @Suppress("UNCHECKED_CAST")
        shadows += shadow as S
    }

    private fun attachToParent() {
        val parent = parentView
        if (parent.isLaidOut) setSize(parent.width, parent.height)
        parent.addOnLayoutChangeListener(this)
        addToOverlay(parent.overlay)
    }

    abstract fun addToOverlay(overlay: ViewGroupOverlay)

    protected fun checkAddProjectionReceiver() {
        if (parentView.background == null) parentView.background = EmptyDrawable
    }

    fun removeShadow(shadow: Shadow) {
        @Suppress("UNCHECKED_CAST")
        shadows -= shadow as S
        if (shadows.isEmpty()) detachFromParent()
    }

    private fun detachFromParent() {
        val parent = parentView
        parent.removeOnLayoutChangeListener(this)
        removeFromOverlay(parent.overlay)
    }

    abstract fun removeFromOverlay(overlay: ViewGroupOverlay)

    protected fun checkRemoveProjectionReceiver() {
        if (parentView.background == EmptyDrawable) parentView.background = null
    }

    override fun onLayoutChange(
        v: View?,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        oldLeft: Int,
        oldTop: Int,
        oldRight: Int,
        oldBottom: Int
    ) {
        setSize(right - left, bottom - top)
    }

    open fun setSize(width: Int, height: Int) {}

    abstract fun updateShadowsAndInvalidate()
}


internal sealed class Shadow(
    protected val targetView: View,
    private val controller: ShadowController,
    private val plane: ContainerPlane<*>
) {
    protected val originalProvider: ViewOutlineProvider = targetView.outlineProvider

    protected val clipPath = Path()

    val willDraw: Boolean
        get() = targetView.isVisible && !clipPath.isEmpty

    @CallSuper
    open fun attachToTarget() {
        targetView.shadow = this
        wrapOutlineProvider(targetView)
        plane.addShadow(this)
    }

    open fun wrapOutlineProvider(targetView: View) {
        targetView.outlineProvider = CallbackProviderWrapper(originalProvider, this)
    }

    @CallSuper
    open fun detachFromTarget() {
        targetView.shadow = null
        targetView.outlineProvider = originalProvider
        plane.removeShadow(this)
    }

    @CallSuper
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

    @CallSuper
    open fun show() {
        plane.addShadow(this)
    }

    @CallSuper
    open fun hide() {
        plane.removeShadow(this)
    }

    fun notifyDetach() {
        controller.removeShadow(this)
    }

    fun notifyAttributeChanged() {
        controller.notifyAttributeChanged(this, targetView)
    }

    abstract fun update(): Boolean
}


internal var ViewGroup.shadowController: ShadowController?
    get() = getTag(R.id.tag_parent_shadow_controller) as? ShadowController
    set(value) {
        setTag(R.id.tag_parent_shadow_controller, value)
    }

internal fun getOrCreateController(parentView: ViewGroup) =
    parentView.shadowController ?: ShadowController(parentView).apply { attachToParent() }

private val CacheRect = Rect()
private val CacheRectF = RectF()