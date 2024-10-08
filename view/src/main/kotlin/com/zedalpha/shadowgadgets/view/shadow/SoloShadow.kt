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
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable
import com.zedalpha.shadowgadgets.view.forceShadowLayer
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.pathProvider

internal class SoloShadow(targetView: View) : ViewShadow(targetView) {

    private var parentView: View? = null

    private val drawable = object : ShadowDrawable(targetView, isClipped) {

        override fun draw(canvas: Canvas) {
            if (!targetView.updateAndCheckDraw(coreShadow) || !isShown) return

            updateBounds()

            canvas.save()
            if (!hasIdentityMatrix()) {
                val matrix = tmpMatrix ?: Matrix().also { tmpMatrix = it }
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
            setBounds(
                -targetView.left,
                -targetView.top,
                -targetView.left + parent.width,
                -targetView.top + parent.height
            )
        }

        private var tmpMatrix: Matrix? = null
    }

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = attach()
        override fun onViewDetachedFromWindow(v: View) = detach()
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (isShown && checkInvalidate()) drawable.invalidateSelf()
        true
    }

    private fun checkInvalidate(): Boolean {
        if (!isShown) return false

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

    private val provider: ViewOutlineProvider = targetView.outlineProvider

    private val isDrawing = canDrawAround(targetView)

    init {
        if (isDrawing) {
            targetView.addOnAttachStateChangeListener(attachListener)
            if (targetView.isAttachedToWindow) attach()
            if (targetView.colorOutlineShadow) {
                drawable.colorCompat = targetView.outlineShadowColorCompat
            }
            drawable.forceLayer = targetView.forceShadowLayer
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
        super.detachFromTarget()
        drawable.dispose()
        if (isDrawing) {
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
        target.pathProvider?.let { pathProvider ->
            drawable.setClipPathProvider { path ->
                pathProvider.getPath(target, path)
            }
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

private fun canDrawAround(view: View): Boolean {
    val clipToOutline = view.clipToOutline
    val clipChildren = (view.parent as? ViewGroup)?.clipChildren == true
    val canDraw = !clipToOutline && !clipChildren
    if (!canDraw && BuildConfig.DEBUG) {
        val message = buildString {
            append("Inline shadow on ${view.debugName}: ")
            if (clipToOutline) append("target has clipToOutline=true")
            if (clipToOutline && clipChildren) append(", and ")
            if (clipChildren) append("parent has clipChildren=true")
        }
        Log.w("ShadowGadgets", message)
    }
    return canDraw
}

private inline val View.debugName: String
    get() = buildString {
        append(this@debugName.javaClass.simpleName)
        if (id == View.NO_ID) return@buildString
        append(" R.id.${resources.getResourceEntryName(id)}")
    }