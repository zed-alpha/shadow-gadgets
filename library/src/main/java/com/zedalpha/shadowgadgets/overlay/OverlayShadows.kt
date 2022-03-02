@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.rendernode.RenderNodeColors
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper
import java.lang.reflect.Field
import java.lang.reflect.Method


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
    protected val originalProvider: ViewOutlineProvider = targetView.outlineProvider
    protected val clipPath = Path()

    val willDraw: Boolean
        get() = targetView.isVisible && !clipPath.isEmpty

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
        val path = clipPath
        path.reset()

        if (outline.isEmpty) return

        val outlineBounds = CacheBounds
        if (getRect(outline, outlineBounds)) {
            val boundsF = CacheBoundsF
            boundsF.set(outlineBounds)
            val outlineRadius = getRadius(outline)
            path.addRoundRect(boundsF, outlineRadius, outlineRadius, Path.Direction.CW)
        } else {
            OutlinePathReflector.setPath(outline, path)
        }
    }

    abstract fun attachShadow()
    abstract fun detachShadow()
    abstract fun onPreDraw()
}


private val getRadius: (Outline) -> Float =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> { outline -> outline.radius }
        OutlineRectReflector.hasRectAccess -> { outline -> OutlineRectReflector.getRadius(outline) }
        else -> { _ -> 0F }
    }

private val getRect: (Outline, Rect) -> Boolean =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> { outline, rect -> outline.getRect(rect) }
        OutlineRectReflector.hasRectAccess -> { outline, rect ->
            OutlineRectReflector.getRect(
                outline,
                rect
            )
        }
        else -> { _, _ -> false }
    }

@SuppressLint("DiscouragedPrivateApi", "SoonBlockedPrivateApi")
private object OutlineRectReflector {
    private val mRectField: Field by lazy { Outline::class.java.getDeclaredField("mRect") }
    private val mRadiusField: Field by lazy { Outline::class.java.getDeclaredField("mRadius") }

    val hasRectAccess = try {
        mRectField
        mRadiusField
        true
    } catch (e: Throwable) {
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

@SuppressLint("DiscouragedPrivateApi")
private object OutlinePathReflector {
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    private val requiresDoubleReflection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    private val getDeclaredField: Method by lazy {
        Class::class.java.getDeclaredMethod(
            "getDeclaredField",
            String::class.java
        )
    }

    private val mPathField: Field by lazy {
        if (requiresDoubleReflection) {
            getDeclaredField.invoke(
                Outline::class.java,
                "mPath"
            ) as Field
        } else {
            Outline::class.java.getDeclaredField("mPath")
        }
    }

    private val hasPathAccess = try {
        mPathField
        true
    } catch (e: Throwable) {
        false
    }

    fun setPath(outline: Outline, path: Path) {
        if (hasPathAccess) {
            val outlinePath = mPathField.get(outline) as? Path ?: return
            path.set(outlinePath)
        }
    }
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
internal val CacheBounds = Rect()
internal val CacheBoundsF = RectF()
internal val CacheMatrix = Matrix()