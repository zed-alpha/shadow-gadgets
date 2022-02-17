@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
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
        getOrCreateControllerForParent(parent).newShadow(targetView)
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
        viewGroup.setTag(R.id.tag_parent_overlay_shadow_controller, this)
        viewGroup.viewTreeObserver.addOnPreDrawListener(this)
    }

    fun detach() {
        viewGroup.setTag(R.id.tag_parent_overlay_shadow_controller, null)
        viewGroup.viewTreeObserver.removeOnPreDrawListener(this)
    }

    fun newShadow(view: View) {
        val shadow = createShadow(view)
        shadow.attachToTargetView()
        onNewShadow(shadow)
        shadows.add(shadow)
    }

    fun createShadow(view: View): T
    fun onNewShadow(shadow: T)

    @Suppress("UNCHECKED_CAST")
    fun removeShadow(view: View) {
        val shadow = view.getTag(R.id.tag_target_overlay_shadow) as T
        shadow.detachFromTargetView()
        onRemoveShadow(shadow)
        shadows.remove(shadow)
        if (shadows.isEmpty()) detach()
    }

    fun onRemoveShadow(shadow: T)

    override fun onPreDraw(): Boolean {
        shadows.forEach { it.update() }
        return true
    }
}

internal sealed class OverlayShadow(
    private val targetView: View,
    private val controller: OverlayController<*>
) {
    private val outlineBounds = Rect()
    private var radius: Float = 0.0F

    protected val originalProvider: ViewOutlineProvider = targetView.outlineProvider

    fun attachToTargetView() {
        val target = targetView
        target.setTag(R.id.tag_target_overlay_shadow, this)
        target.outlineProvider = ProviderWrapper(originalProvider, this::setOutline)
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
            radius = getRadius(outline)
        } else {
            outlineBounds.setEmpty()
            radius = 0.0F
        }
    }

    val z get() = targetView.z

    @Suppress("LiftReturnOrAssignment")
    fun prepareForClip(path: Path, boundsF: RectF): Boolean {
        if (!outlineBounds.isEmpty) {
            // Set path for clip.
            boundsF.set(outlineBounds)
            boundsF.offset(targetView.left.toFloat(), targetView.top.toFloat())
            path.rewind()
            path.addRoundRect(boundsF, radius, radius, Path.Direction.CW)
            return true
        } else {
            return false
        }
    }

    fun update() {
        updateShadow(targetView)
    }

    abstract fun updateShadow(target: View)
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
    private val callback: (Outline) -> Unit
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        callback(outline)
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

internal inline val View.debugName: String
    get() = buildString {
        append(this@debugName::class.java.simpleName).append(", id:")
        if (id == View.NO_ID) append("NO_ID")
        else append(resources.getResourceEntryName(id))
    }

// Assuming single thread for now.
internal val CachePath = Path()
internal val CacheBoundsF = RectF()