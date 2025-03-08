package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewTreeObserver
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.layer.LocationTracker

internal abstract class ShadowController(protected val ownerView: View?) {

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
    protected abstract fun invalidate()
    protected abstract fun checkInvalidate()

    private val layoutListener =
        View.OnLayoutChangeListener { _, l, t, r, b, _, _, _, _ ->
            onSizeChanged(r - l, b - t)
        }

    protected abstract fun onSizeChanged(width: Int, height: Int)

    init {
        attachToParent()
    }

    private fun attachToParent() {
        val owner = ownerView ?: return
        owner.addOnAttachStateChangeListener(attachListener)
        if (owner.isAttachedToWindow) addPreDrawListener()
        owner.addOnLayoutChangeListener(layoutListener)
    }

    @CallSuper
    protected open fun detachFromParent() {
        val owner = ownerView ?: return
        owner.removeOnAttachStateChangeListener(attachListener)
        owner.removeOnLayoutChangeListener(layoutListener)
        removePreDrawListener()
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
        return Layer(coreShadow::draw, owner).also { onCreateLayer(it) }
    }

    fun disposeLayer(layer: Layer) {
        onDisposeLayer(layer)
        layer.dispose()
    }

    protected abstract fun onCreateLayer(layer: Layer)
    protected abstract fun onDisposeLayer(layer: Layer)
}