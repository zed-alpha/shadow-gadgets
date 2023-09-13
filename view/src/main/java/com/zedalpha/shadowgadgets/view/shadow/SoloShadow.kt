package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.BuildConfig
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable
import com.zedalpha.shadowgadgets.view.forceShadowLayer
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.pathProvider


internal class SoloShadow(private val targetView: View) : ViewShadow {

    override var isShown = true

    private var matrix: Matrix? = null

    private var parentView: View? = null

    private val drawable = object : ShadowDrawable(
        targetView,
        targetView.clipOutlineShadow
    ) {
        override fun draw(canvas: Canvas) {
            if (!(isShown && targetView.checkDrawAndUpdate(coreShadow))) return

            updateBounds()

            canvas.save()
            if (!hasIdentityMatrix()) {
                val matrix = matrix ?: Matrix().also { matrix = it }
                getMatrix(matrix)
                matrix.invert(matrix)
                canvas.concat(matrix)
            }
            canvas.translate(bounds.left.toFloat(), bounds.top.toFloat())
            super.draw(canvas)
            canvas.restore()
        }

        fun updateBounds() {
            val parent = parentView ?: return
            val target = targetView
            setBounds(
                -target.left,
                -target.top,
                -target.left + parent.width,
                -target.top + parent.height
            )
        }
    }

    private val attachListener =
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                attach()
            }

            override fun onViewDetachedFromWindow(v: View) {
                detach()
            }
        }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (isShown && checkInvalidate()) drawable.invalidateSelf()
        true
    }

    private fun checkInvalidate(): Boolean {
        val target = targetView
        val shadow = drawable.coreShadow

        if (shadow.alpha != target.alpha) return true
        if (shadow.cameraDistance != target.cameraDistance) return true
        if (shadow.elevation != target.elevation) return true
        if (shadow.translationZ != target.translationZ) return true
        if (Build.VERSION.SDK_INT >= 28) {
            if (shadow.ambientColor !=
                ViewShadowColorsHelper.getAmbientColor(target)
            ) return true
            if (shadow.spotColor !=
                ViewShadowColorsHelper.getSpotColor(target)
            ) return true
        }
        return false
    }

    private val provider = targetView.outlineProvider

    private val isEnabled = canDrawAround(targetView)

    init {
        val target = targetView
        target.shadow = this

        if (isEnabled) {
            target.addOnAttachStateChangeListener(attachListener)
            if (target.isAttachedToWindow) attach()
            if (target.colorOutlineShadow) {
                drawable.colorCompat = target.outlineShadowColorCompat
            }
            drawable.forceLayer = target.forceShadowLayer
        } else {
            // Still disable the native shadow, because no shadow is better
            // than a mangled draw, possibly with the artifact still there.
            targetView.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) {
                    provider.getOutline(view, outline)
                    outline.alpha = 0F
                }
            }
        }
    }

    override fun detachFromTarget() {
        targetView.shadow = null
        drawable.dispose()
        if (isEnabled) {
            targetView.removeOnAttachStateChangeListener(attachListener)
            detach()
        } else {
            targetView.outlineProvider = provider
        }
    }

    private var viewTreeObserver: ViewTreeObserver? = null

    private fun attach() {
        val target = targetView
        val parent = target.parent as? View ?: return
        parentView = parent

        drawable.updateBounds()
        target.overlay.add(drawable)

        viewTreeObserver = target.viewTreeObserver.also { observer ->
            observer.addOnPreDrawListener(preDrawListener)
        }

        // Must set before outlineProvider
        drawable.setClipPathProvider { path ->
            target.pathProvider?.getPath(target, path)
        }
        target.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                provider.getOutline(view, outline)
                drawable.setOutline(outline)
                outline.alpha = 0F
            }
        }
        drawable.invalidateSelf()
    }

    private fun detach() {
        drawable.setClipPathProvider(null)
        targetView.overlay.remove(drawable)
        targetView.outlineProvider = provider
        viewTreeObserver?.run {
            if (isAlive) removeOnPreDrawListener(preDrawListener)
            viewTreeObserver = null
        }
        parentView = null
    }

    override fun checkRecreate() =
        targetView.clipOutlineShadow != drawable.isClipped

    override fun updateColorCompat(color: Int) {
        drawable.run {
            if (colorCompat == color) return
            colorCompat = color
            invalidateSelf()
        }
    }

    override fun invalidate() {
        drawable.invalidateSelf()
    }
}

private fun canDrawAround(targetView: View): Boolean {
    val clipChildren = (targetView.parent as? ViewGroup)?.clipChildren == true
    val clipToOutline = targetView.clipToOutline
    val canDraw = !clipChildren && !clipToOutline
    if (!canDraw && BuildConfig.DEBUG) {
        val message = buildString {
            append("Inline shadow on ${targetView.debugName}: Added ")
            if (clipChildren) append("in parent with clipChildren=true")
            if (clipChildren && clipToOutline) append(", and ")
            if (clipToOutline) append("on target with clipToOutline=true")
        }
        Log.w("ShadowGadgets", message)
    }
    return canDraw
}

private val View.debugName: String
    get() = buildString {
        append(this@debugName.javaClass.simpleName)
        if (id == View.NO_ID) return@buildString
        append(" R.id.${resources.getResourceEntryName(id)}")
    }