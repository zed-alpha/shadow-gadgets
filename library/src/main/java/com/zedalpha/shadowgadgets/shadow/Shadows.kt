@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.ClippedShadowPlane
import com.zedalpha.shadowgadgets.ClippedShadowPlane.Foreground
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory


internal class ShadowController(private val parentView: ViewGroup) :
    View.OnAttachStateChangeListener, View.OnLayoutChangeListener,
    ViewTreeObserver.OnDrawListener {

    private val shadowContainer = if (RenderNodeFactory.isOpenForBusiness) {
        RenderNodeShadowContainer(parentView, this)
    } else {
        ViewShadowContainer(parentView, this)
    }

    fun attachToParent() {
        val parent = parentView
        parent.shadowController = this
        if (parent.isAttachedToWindow) addLayoutDrawListeners()
        parent.addOnAttachStateChangeListener(this)
        shadowContainer.attachToParent()
    }

    fun detachFromParent() {
        val parent = parentView
        parent.shadowController = null
        if (parent.isAttachedToWindow) removeLayoutDrawListeners()
        parent.removeOnAttachStateChangeListener(this)
        shadowContainer.detachFromParent()
    }

    fun addShadowForView(targetView: View) {
        if (targetView.outlineProvider != null) {
            shadowContainer.addShadowForView(targetView)
        }
    }

    fun removeShadow(shadow: Shadow) {
        shadowContainer.removeShadow(shadow)
    }

    fun notifyAttributeChanged(shadow: Shadow, targetView: View) {
        removeShadow(shadow)
        // Current instance might've detached in removeShadow; must getOrCreate.
        getOrCreateController(parentView).addShadowForView(targetView)
    }

    fun refreshAll() {
        shadowContainer.refreshAll()
    }

    override fun onViewAttachedToWindow(v: View) {
        addLayoutDrawListeners()
    }

    override fun onViewDetachedFromWindow(v: View) {
        removeLayoutDrawListeners()
    }

    override fun onDraw() {
        shadowContainer.updateAndInvalidateShadows()
    }

    override fun onLayoutChange(
        v: View,
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

    private fun addLayoutDrawListeners() {
        val parent = parentView
        if (parent.isLaidOut) setSize(parent.width, parent.height)
        parent.addOnLayoutChangeListener(this)
        parent.viewTreeObserver.addOnDrawListener(this)
    }

    private fun removeLayoutDrawListeners() {
        val parent = parentView
        parent.removeOnLayoutChangeListener(this)
        parent.viewTreeObserver.removeOnDrawListener(this)
    }

    private fun setSize(width: Int, height: Int) {
        shadowContainer.setSize(width, height)
    }
}


@Suppress("UNCHECKED_CAST")
internal sealed interface Plane<S : Shadow> {
    val shadows: MutableList<S>

    fun addShadow(shadow: Shadow) {
        shadows += shadow as S
    }

    fun removeShadow(shadow: Shadow) {
        shadows -= shadow as S
    }

    fun isEmpty() = shadows.isEmpty()

    fun updateAndInvalidateShadows()
}


internal sealed class ShadowContainer<S : Shadow, P : Plane<S>>(
    protected val parentView: ViewGroup,
    protected val controller: ShadowController
) {
    abstract val backgroundPlane: P
    abstract val foregroundPlane: P

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

    fun updateAndInvalidateShadows() {
        backgroundPlane.updateAndInvalidateShadows()
        foregroundPlane.updateAndInvalidateShadows()
    }

    fun refreshAll() {
        backgroundPlane.shadows.forEach { it.notifyAttributeChanged() }
        foregroundPlane.shadows.forEach { it.notifyAttributeChanged() }
    }

    abstract fun attachToParent()
    abstract fun detachFromParent()
    abstract fun setSize(width: Int, height: Int)
    abstract fun createShadow(targetView: View, plane: P): S
    abstract fun determinePlane(targetView: View): ClippedShadowPlane
}


internal sealed class Shadow(
    protected val targetView: View,
    private val controller: ShadowController,
    private val plane: Plane<*>
) {
    protected val originalProvider: ViewOutlineProvider = targetView.outlineProvider

    protected val clipPath = Path()

    val willDraw: Boolean
        get() = targetView.isVisible && !clipPath.isEmpty

    @CallSuper
    open fun attachToTarget() {
        val target = targetView
        target.shadow = this
        wrapOutlineProvider(target)
        plane.addShadow(this)
    }

    open fun wrapOutlineProvider(targetView: View) {
        targetView.outlineProvider = CallbackProviderWrapper(originalProvider, this)
    }

    @CallSuper
    open fun detachFromTarget() {
        val target = targetView
        target.shadow = null
        target.outlineProvider = originalProvider
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