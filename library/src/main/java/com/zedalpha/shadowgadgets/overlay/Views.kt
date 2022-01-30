package com.zedalpha.shadowgadgets.overlay

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import com.zedalpha.shadowgadgets.overlay.OverlayController.Companion.cacheBoundsF
import com.zedalpha.shadowgadgets.overlay.OverlayController.Companion.cachePath
import com.zedalpha.shadowgadgets.shadowXmlAttributes


@SuppressLint("ViewConstructor")
internal class ViewController(override val viewGroup: ViewGroup) :
    ViewGroup(viewGroup.context), View.OnLayoutChangeListener, OverlayController<ViewShadow> {

    private val relay = InvalidateRelayDrawable(this)

    override val shadows = mutableListOf<ViewShadow>()

    init {
        val group = viewGroup
        if (group.isLaidOut) layout(0, 0, group.width, group.height)
        group.addOnLayoutChangeListener(this)
        group.overlay.add(this)
        group.overlay.add(relay)
    }

    override fun detach() {
        super.detach()
        val group = viewGroup
        group.removeOnLayoutChangeListener(this)
        group.overlay.remove(this)
        group.overlay.remove(relay)
    }

    override fun createShadow(view: View) = ViewShadow(view)

    override fun onNewShadow(shadow: ViewShadow) {
        shadow.addShadowView(this)
    }

    override fun onRemoveShadow(shadow: ViewShadow) {
        shadow.removeShadowView(this)
    }

    override fun dispatchDraw(canvas: Canvas) {
        val saveCount = canvas.save()
        shadows.sortedBy { it.z }.forEach {
            val path = cachePath
            if (it.prepareForDraw(path, cacheBoundsF)) clipOutPath(canvas, path)
        }
        super.dispatchDraw(canvas)
        canvas.restoreToCount(saveCount)
    }

    override fun onLayoutChange(
        v: View, l: Int, t: Int, r: Int, b: Int, ol: Int, ot: Int, or: Int, ob: Int
    ) {
        val newWidth = r - l
        val newHeight = b - t
        if (width != newWidth || height != newHeight) {
            layout(0, 0, newWidth, newHeight)
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        /* noop. Child layout is handled manually elsewhere. */
    }
}

@SuppressLint("ClickableViewAccessibility")
internal class ViewShadow(view: View) : OverlayShadow(view), View.OnTouchListener {
    private val shadowView = View(view.context).apply {
        background = EmptyDrawable
        outlineProvider = SurrogateViewProviderWrapper(originalProvider, view)
        clipToOutline = true
        elevation = view.elevation
        translationZ = view.translationZ
    }

    init {
        val animator = view.stateListAnimator
        if (view.shadowXmlAttributes?.disableAnimation != true && animator != null) {
            shadowView.stateListAnimator = animator.clone()
            view.setOnTouchListener(this)
        }
    }

    override fun onTouch(view: View, event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                shadowView.isPressed = true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                shadowView.isPressed = false
            }
        }
        return false
    }

    override fun update(
        left: Int, top: Int, right: Int, bottom: Int,
        elevation: Float, translationZ: Float
    ) {
        shadowView.layout(left, top, right, bottom)
        shadowView.elevation = elevation
        shadowView.translationZ = translationZ
    }

    fun addShadowView(controller: ViewController) {
        controller.addView(shadowView)
    }

    fun removeShadowView(controller: ViewController) {
        controller.removeView(shadowView)
    }
}

private object EmptyDrawable : Drawable() {
    override fun draw(canvas: Canvas) {}
    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(filter: ColorFilter?) {}
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

private class InvalidateRelayDrawable(private val controller: ViewController) : Drawable() {
    override fun draw(canvas: Canvas) {
        controller.invalidate()
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(filter: ColorFilter?) {}
    override fun getOpacity() = PixelFormat.TRANSLUCENT
}

private class SurrogateViewProviderWrapper(
    private val provider: ViewOutlineProvider,
    private val surrogate: View
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        provider.getOutline(surrogate, outline)
    }
}