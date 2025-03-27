package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.layer.LocationTracker

internal abstract class ShadowController(
    protected val ownerView: View?,
    protected val scopeView: View?
) {
    private val attachListener = object : View.OnAttachStateChangeListener {

        override fun onViewAttachedToWindow(v: View) {
            addPreDrawListener()
            onParentViewAttached()
        }

        override fun onViewDetachedFromWindow(v: View) {
            removePreDrawListener()
            onParentViewDetached()
        }
    }

    protected open fun onParentViewAttached() {}
    protected open fun onParentViewDetached() {}

    private var tracker: LocationTracker? = null

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (hasColorLayer() && tracker?.checkLocationChanged() == true) {
            recreateColorLayers()
            invalidate()
        } else {
            checkInvalidate()
        }
        true
    }

    protected abstract fun hasColorLayer(): Boolean
    protected abstract fun recreateColorLayers()
    protected abstract fun checkInvalidate()
    protected abstract fun invalidate()

    private val scopeLayoutListener =
        if (scopeView != null) {
            View.OnLayoutChangeListener { _, l, t, r, b, _, _, _, _ ->
                onLayerSizeChanged(r - l, b - t)
            }
        } else {
            null
        }

    protected open fun onLayerSizeChanged(width: Int, height: Int) {}

    init {
        attachToOwner()
    }

    private fun attachToOwner() {
        val owner = ownerView ?: return
        owner.addOnAttachStateChangeListener(attachListener)
        if (owner.isAttachedToWindow) addPreDrawListener()
        scopeView?.addOnLayoutChangeListener(scopeLayoutListener)
    }

    @CallSuper
    protected open fun detachFromOwner() {
        val owner = ownerView ?: return
        owner.removeOnAttachStateChangeListener(attachListener)
        removePreDrawListener()
        scopeView?.removeOnLayoutChangeListener(scopeLayoutListener)
    }

    private var viewTreeObserver: ViewTreeObserver? = null

    private fun addPreDrawListener() {
        val owner = ownerView ?: return
        viewTreeObserver = owner.viewTreeObserver.apply {
            if (isAlive) addOnPreDrawListener(preDrawListener)
        }
        tracker = LocationTracker(owner).apply { initialize() }
    }

    private fun removePreDrawListener() {
        viewTreeObserver?.run {
            if (isAlive) removeOnPreDrawListener(preDrawListener)
            viewTreeObserver = null
        }
    }

    fun obtainLayer(coreShadow: Shadow): Layer? {
        val owner = ownerView ?: return null
        val layer = Layer(coreShadow::draw, owner)
        onCreateLayer(layer)
        return layer
    }

    fun disposeLayer(layer: Layer) {
        onDisposeLayer(layer)
        layer.dispose()
    }

    protected abstract fun onCreateLayer(layer: Layer)
    protected abstract fun onDisposeLayer(layer: Layer)
}