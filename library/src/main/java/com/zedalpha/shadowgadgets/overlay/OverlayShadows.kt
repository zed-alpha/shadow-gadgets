package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
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
    if (targetView.outlineProvider != null) {
        val parent = targetView.parent as? ViewGroup ?: return
        getOrCreateControllerForParent(parent).newShadow(targetView)
    } else {
        Log.w("OverlayShadow", "No ViewOutlineProvider on target ${targetView.debugName}")
    }
}

internal fun removeShadowFromOverlay(targetView: View) {
    val parent = targetView.parent as? ViewGroup ?: return
    getControllerForParent(parent)?.removeShadow(targetView)
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


internal sealed interface OverlayController<T : OverlayShadow> {
    val viewGroup: ViewGroup
    val shadows: MutableList<T>

    fun attach() {
        viewGroup.setTag(R.id.tag_parent_overlay_shadow_controller, this)
    }

    fun detach() {
        viewGroup.setTag(R.id.tag_parent_overlay_shadow_controller, null)
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

    companion object {
        val cachePath = Path()
        val cacheBoundsF = RectF()
    }
}

internal sealed class OverlayShadow(protected val targetView: View) {
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

    fun prepareForDraw(path: Path, boundsF: RectF): Boolean {
        if (!outlineBounds.isEmpty) {
            // Update shadow.
            val target = targetView
            update(
                target.left, target.top, target.right, target.bottom,
                target.elevation, target.translationZ
            )

            // Set path for clip.
            boundsF.set(outlineBounds)
            boundsF.offset(target.left.toFloat(), target.top.toFloat())
            path.rewind()
            path.addRoundRect(boundsF, radius, radius, Path.Direction.CW)
            return true
        } else {
            return false
        }
    }

    abstract fun update(
        left: Int, top: Int, right: Int, bottom: Int,
        elevation: Float, translationZ: Float
    )
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
    val isValid: Boolean

    private val mRectField: Field by lazy { Outline::class.java.getDeclaredField("mRect") }
    private val mRadiusField: Field by lazy { Outline::class.java.getDeclaredField("mRadius") }

    init {
        isValid = try {
            mRectField
            mRadiusField
            true
        } catch (e: Exception) {
            false
        }
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