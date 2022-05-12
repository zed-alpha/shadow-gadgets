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
import com.zedalpha.shadowgadgets.clippedShadowPlane
import com.zedalpha.shadowgadgets.isClippedShadowPlaneExplicitlySet
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.viewgroup.ClippedShadowsViewGroup
import com.zedalpha.shadowgadgets.viewgroup.clippedShadowsLayoutParams


internal abstract class ShadowController<S : Shadow, C : ShadowContainer<S>>(
    private val parentView: ViewGroup
) : View.OnAttachStateChangeListener, View.OnLayoutChangeListener, ViewTreeObserver.OnDrawListener {

    abstract val shadowContainer: C

    abstract fun createShadowForView(view: View): S

    fun attachToParent() {
        val parent = parentView
        parentView.shadowController = this
        if (parent.isAttachedToWindow) addLayoutDrawListeners()
        parent.addOnAttachStateChangeListener(this)
        shadowContainer.attachToParent()
    }

    fun detachFromParent() {
        val parent = parentView
        parentView.shadowController = null
        if (parent.isAttachedToWindow) removeLayoutDrawListeners()
        parent.removeOnAttachStateChangeListener(this)
        shadowContainer.detachFromParent()
    }

    fun addShadowForView(targetView: View) {
        if (targetView.outlineProvider != null) {
            val shadow = createShadowForView(targetView)
            shadowContainer.addShadow(shadow)
            shadow.attachToTarget()
        }
    }

    fun removeShadow(shadow: Shadow) {
        @Suppress("UNCHECKED_CAST")
        shadowContainer.removeShadow(shadow as S)
        shadow.detachFromTarget()
    }

    fun notifyAttributeChanged(shadow: Shadow, targetView: View) {
        removeShadow(shadow)
        // Current instance might've detached in removeShadow; must getOrCreate.
        getOrCreateController(parentView).addShadowForView(targetView)
    }

    fun refreshAll() {
        shadowContainer.shadows.forEach { it.notifyAttributeChanged() }
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


internal sealed class ShadowContainer<S : Shadow>(
    protected val parentView: ViewGroup,
    private val controller: ShadowController<*, *>
) {
    val shadows = mutableListOf<S>()

    @CallSuper
    open fun addShadow(shadow: S) {
        shadows += shadow
    }

    @CallSuper
    open fun removeShadow(shadow: S) {
        shadows -= shadow
        if (shadows.isEmpty()) controller.detachFromParent()
    }

    fun updateAndInvalidateShadows() {
        var invalidate = false
        shadows.forEach { if (it.update()) invalidate = true }
        if (invalidate) parentView.invalidate()
    }

    abstract fun attachToParent()
    abstract fun detachFromParent()
    abstract fun setSize(width: Int, height: Int)
}


internal sealed class Shadow(
    protected val targetView: View,
    private val shadowController: ShadowController<*, *>
) {
    protected val originalProvider: ViewOutlineProvider = targetView.outlineProvider

    protected val clipPath = Path()

    private val clippedShadowPlane: ClippedShadowPlane

    init {
        val shadowsParent = targetView.parent as? ClippedShadowsViewGroup
        clippedShadowPlane =
            if (shadowsParent != null && !targetView.isClippedShadowPlaneExplicitlySet) {
                targetView.clippedShadowsLayoutParams?.clippedShadowPlane
                    ?: shadowsParent.childClippedShadowsPlane
            } else {
                targetView.clippedShadowPlane
            }
    }

    val isForeground = clippedShadowPlane == Foreground

    val willDraw: Boolean
        get() = targetView.isVisible && !clipPath.isEmpty

    @CallSuper
    open fun attachToTarget() {
        val target = targetView
        wrapOutlineProvider(target)
        target.shadow = this
    }

    open fun wrapOutlineProvider(targetView: View) {
        targetView.outlineProvider = CallbackProviderWrapper(originalProvider, this)
    }

    @CallSuper
    open fun detachFromTarget() {
        val target = targetView
        target.shadow = null
        target.outlineProvider = originalProvider
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

    fun notifyDetach() {
        shadowController.removeShadow(this)
    }

    fun notifyAttributeChanged() {
        shadowController.notifyAttributeChanged(this, targetView)
    }

    abstract fun update(): Boolean
    abstract fun show()
    abstract fun hide()
}


internal var ViewGroup.shadowController: ShadowController<*, *>?
    get() = getTag(R.id.tag_parent_shadow_controller) as? ShadowController<*, *>
    set(value) {
        setTag(R.id.tag_parent_shadow_controller, value)
    }

internal fun getOrCreateController(parentView: ViewGroup) =
    parentView.shadowController ?: createController(parentView).apply { attachToParent() }

private val createController: (ViewGroup) -> ShadowController<*, *> =
    if (RenderNodeFactory.isOpenForBusiness) {
        ::RenderNodeShadowController
    } else {
        ::ViewShadowController
    }

private val CacheRect = Rect()
private val CacheRectF = RectF()