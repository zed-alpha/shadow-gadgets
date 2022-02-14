package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.*
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.animateShadowWhenClipped
import com.zedalpha.shadowgadgets.rendernode.CanvasReflector
import com.zedalpha.shadowgadgets.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@SuppressLint("ViewConstructor")
internal class RenderNodeController(override val viewGroup: ViewGroup) :
    View(viewGroup.context), View.OnTouchListener, OverlayController<RenderNodeShadow> {

    override val shadows = mutableListOf<RenderNodeShadow>()

    override fun createShadow(view: View) = RenderNodeShadow(view)

    override fun newShadow(view: View) {
        super.newShadow(view)
        shadows.sortedBy { it.z }.forEach { it.addToOverlay(viewGroup.overlay) }
    }

    override fun onNewShadow(shadow: RenderNodeShadow) {
        shadows.forEach { it.removeFromOverlay(viewGroup.overlay) }
        shadow.registerForTouchDispatch(this)
    }

    override fun onRemoveShadow(shadow: RenderNodeShadow) {
        shadow.removeFromOverlay(viewGroup.overlay)
        shadow.unregisterFromTouchDispatch()
    }

    private var currentShadow: RenderNodeShadow? = null
    override fun onTouch(view: View, event: MotionEvent): Boolean {
        val shadow = currentShadow
        if (shadow == null || !shadow.isForView(view)) {
            currentShadow = shadows.firstOrNull { it.isForView(view) }
            stateListAnimator = view.stateListAnimator?.clone()
            elevation = view.elevation
            translationZ = view.translationZ
        }

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPressed = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPressed = false
            }
        }
        return false
    }

    override fun setElevation(elevation: Float) {
        currentShadow?.setElevation(elevation)
    }

    override fun setTranslationZ(translationZ: Float) {
        currentShadow?.setTranslationZ(translationZ)
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal class RenderNodeShadow(view: View) : OverlayShadow(view) {
    private val renderNodeDrawable = when {
        UsesPublicApi -> PublicApiDrawable(this)
        UsesStubs -> StubDrawable(this)
        else -> ReflectorDrawable(this)
    }

    override fun setOutline(outline: Outline) {
        super.setOutline(outline)
        renderNodeDrawable.setOutline(if (outline.isEmpty) null else outline)
    }

    fun addToOverlay(overlay: ViewOverlay) {
        overlay.add(renderNodeDrawable)
    }

    fun removeFromOverlay(overlay: ViewOverlay) {
        overlay.remove(renderNodeDrawable)
    }

    private var registeredForDispatch = false
    fun registerForTouchDispatch(controller: RenderNodeController) {
        val target = targetView
        if (target.stateListAnimator != null && target.animateShadowWhenClipped) {
            target.setOnTouchListener(controller)
            registeredForDispatch = true
        }
    }

    fun unregisterFromTouchDispatch() {
        if (registeredForDispatch) targetView.setOnTouchListener(null)
    }

    fun setElevation(elevation: Float) {
        renderNodeDrawable.setElevation(elevation)
    }

    fun setTranslationZ(translationZ: Float) {
        renderNodeDrawable.setTranslationZ(translationZ)
    }

    fun isForView(view: View) = targetView == view

    override fun updateShadow(target: View) {
        renderNodeDrawable.setBounds(target.left, target.top, target.right, target.bottom)
        renderNodeDrawable.setElevation(target.elevation)
        renderNodeDrawable.setTranslationZ(target.translationZ)
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private open class ReflectorDrawable(shadow: RenderNodeShadow) : RenderNodeShadowDrawable(shadow) {
    override fun clipAndDraw(canvas: Canvas, path: Path, boundsF: RectF) {
        canvas.save()
        clipOutPath(canvas, path)
        CanvasReflector.insertReorderBarrier(canvas)
        renderNodeWrapper.draw(canvas)
        CanvasReflector.insertInorderBarrier(canvas)
        canvas.restore()
    }
}

@RequiresApi(Build.VERSION_CODES.M)
private class StubDrawable(shadow: RenderNodeShadow) : RenderNodeShadowDrawable(shadow) {
    override fun clipAndDraw(canvas: Canvas, path: Path, boundsF: RectF) {
        canvas as DisplayListCanvas

        canvas.save()
        clipOutPath(canvas, path)
        canvas.insertReorderBarrier()
        renderNodeWrapper.draw(canvas)
        canvas.insertInorderBarrier()
        canvas.restore()
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private class PublicApiDrawable(shadow: RenderNodeShadow) : RenderNodeShadowDrawable(shadow) {
    override fun clipAndDraw(canvas: Canvas, path: Path, boundsF: RectF) {
        canvas.save()
        canvas.enableZ()
        canvas.clipOutPath(path)
        renderNodeWrapper.draw(canvas)
        canvas.disableZ()
        canvas.restore()
    }
}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
internal sealed class RenderNodeShadowDrawable(private val shadow: RenderNodeShadow) : Drawable() {
    protected val renderNodeWrapper: RenderNodeWrapper = RenderNodeFactory.newInstance()

    fun setElevation(elevation: Float) {
        renderNodeWrapper.setElevation(elevation)
    }

    fun setTranslationZ(translationZ: Float) {
        renderNodeWrapper.setTranslationZ(translationZ)
    }

    fun setOutline(outline: Outline?) {
        renderNodeWrapper.setOutline(outline)
    }

    override fun onBoundsChange(bounds: Rect) {
        renderNodeWrapper.setPosition(bounds.left, bounds.top, bounds.right, bounds.bottom)
    }

    final override fun draw(canvas: Canvas) {
        val path = CachePath
        val boundsF = CacheBoundsF
        if (shadow.prepareForDraw(path, boundsF)) clipAndDraw(canvas, path, boundsF)
    }

    abstract fun clipAndDraw(canvas: Canvas, path: Path, boundsF: RectF)

    override fun getOpacity() = PixelFormat.TRANSLUCENT

    override fun setAlpha(alpha: Int) {}

    override fun setColorFilter(filter: ColorFilter?) {}
}

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.Q)
private val UsesPublicApi = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.M)
private val UsesStubs = Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.O_MR1