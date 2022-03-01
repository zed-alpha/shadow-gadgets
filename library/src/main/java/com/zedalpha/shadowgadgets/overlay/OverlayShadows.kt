@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.rendernode.RenderNodeColors
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper
import java.lang.reflect.Field


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
    val controller = if (RenderNodeFactory.isOpenForBusiness) {
        RenderNodeController(parent)
    } else {
        ViewController(parent)
    }
    controller.attach()
    return controller
}

internal sealed interface OverlayController<T : OverlayShadow> :
    ViewTreeObserver.OnPreDrawListener {

    val viewGroup: ViewGroup
    val shadows: MutableList<T>

    fun attach() {
        val group = viewGroup
        group.setTag(R.id.tag_parent_overlay_shadow_controller, this)
        group.viewTreeObserver.addOnPreDrawListener(this)
    }

    fun detach() {
        val group = viewGroup
        group.setTag(R.id.tag_parent_overlay_shadow_controller, null)
        group.viewTreeObserver.removeOnPreDrawListener(this)
    }

    fun addShadow(view: View) {
        val shadow = createShadow(view)
        shadow.attachToTargetView()
        shadow.attachShadow()
        shadows += shadow
    }

    @Suppress("UNCHECKED_CAST")
    fun removeShadow(view: View) {
        val shadow = view.getTag(R.id.tag_target_overlay_shadow) as T
        shadow.detachFromTargetView()
        shadow.detachShadow()
        shadows -= shadow
        if (shadows.isEmpty()) detach()
    }

    fun createShadow(view: View): T

    override fun onPreDraw(): Boolean {
        shadows.forEach { it.onPreDraw() }
        return true
    }
}

internal sealed class OverlayShadow(
    private val controller: OverlayController<*>,
    protected val targetView: View
) {
    protected val outlineBounds = Rect()
    protected var outlineRadius: Float = 0.0F

    protected val originalProvider: ViewOutlineProvider = targetView.outlineProvider

    val willDraw: Boolean
        get() = targetView.isVisible && !outlineBounds.isEmpty

    fun attachToTargetView() {
        val target = targetView
        target.setTag(R.id.tag_target_overlay_shadow, this)
        target.outlineProvider = ProviderWrapper(originalProvider, this)
    }

    fun detachFromTargetView() {
        val target = targetView
        target.setTag(R.id.tag_target_overlay_shadow, null)
        target.outlineProvider = originalProvider
    }

    fun detachFromController() {
        controller.removeShadow(targetView)
    }

    @Suppress("LiftReturnOrAssignment")
    open fun setOutline(outline: Outline) {
        if (getRect(outline, outlineBounds)) {
            // outlineBounds set in getRect()
            outlineRadius = getRadius(outline)
        } else {
            outlineBounds.setEmpty()
            outlineRadius = 0.0F
        }
    }

    abstract fun attachShadow()
    abstract fun detachShadow()
    abstract fun onPreDraw()
}


private val getRadius: (Outline) -> Float =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> { outline -> outline.radius }
        OutlineReflector.isValid -> { outline -> OutlineReflector.getRadius(outline) }
        else -> { _ -> 0F }
    }

private val getRect: (Outline, Rect) -> Boolean =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> { outline, rect -> outline.getRect(rect) }
        OutlineReflector.isValid -> { outline, rect -> OutlineReflector.getRect(outline, rect) }
        else -> { _, _ -> false }
    }

@SuppressLint("DiscouragedPrivateApi", "SoonBlockedPrivateApi")
private object OutlineReflector {
    private val mRectField: Field by lazy { Outline::class.java.getDeclaredField("mRect") }
    private val mRadiusField: Field by lazy { Outline::class.java.getDeclaredField("mRadius") }

    val isValid = try {
        mRectField
        mRadiusField
        true
    } catch (e: Error) {
        false
    }

    fun getRect(outline: Outline, outRect: Rect): Boolean {
        val rect = mRectField.get(outline) as Rect?
        return rect?.let {
            outRect.set(rect)
            true
        } ?: false
    }

    fun getRadius(outline: Outline) = mRadiusField.getFloat(outline)
}

private class ProviderWrapper(
    private val wrapped: ViewOutlineProvider,
    private val shadow: OverlayShadow
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        shadow.setOutline(outline)
        outline.alpha = 0.0F
    }
}


internal val clipOutPath: (Canvas, Path) -> Unit =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { canvas, path ->
        canvas.clipOutPath(path)
    } else { canvas, path ->
        @Suppress("DEPRECATION")
        canvas.clipPath(path, Region.Op.DIFFERENCE)
    }

@RequiresApi(Build.VERSION_CODES.P)
internal object ShadowColorsHelper {
    @DoNotInline
    fun changeColors(renderNode: RenderNodeWrapper, target: View): Boolean {
        renderNode as RenderNodeColors
        return renderNode.setAmbientShadowColor(target.outlineAmbientShadowColor) or
                renderNode.setSpotShadowColor(target.outlineSpotShadowColor)
    }

    @DoNotInline
    fun changeColors(shadow: View, target: View): Boolean {
        var changed = false
        if (shadow.outlineAmbientShadowColor != target.outlineAmbientShadowColor) {
            shadow.outlineAmbientShadowColor = target.outlineAmbientShadowColor
            changed = true
        }
        if (shadow.outlineSpotShadowColor != target.outlineSpotShadowColor) {
            shadow.outlineSpotShadowColor = target.outlineSpotShadowColor
            changed = true
        }
        return changed
    }
}

// Assuming single thread for now.
internal val CachePath = Path()
internal val CacheBoundsF = RectF()
internal val CacheMatrix = Matrix()