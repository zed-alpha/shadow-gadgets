package com.zedalpha.shadowgadgets.view.drawable

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewTreeObserver
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.layer.LayerDraw
import com.zedalpha.shadowgadgets.core.layer.LocationTracker

internal class SoloLayer(
    private val drawable: Drawable,
    private val ownerView: View,
    layerDraw: LayerDraw,
    color: Int,
) {
    private val layer = Layer(
        ownerView,
        color,
        drawable.bounds.width(),
        drawable.bounds.height()
    ).apply { addDraw(layerDraw) }

    private val tracker = LocationTracker(ownerView)

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            addPreDrawListener()
        }

        override fun onViewDetachedFromWindow(v: View) {
            removePreDrawListener()
        }
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (tracker.checkLocationChanged()) {
            layer.recreate()
            drawable.invalidateSelf()
        }
        true
    }

    var color by layer::color

    init {
        ownerView.addOnAttachStateChangeListener(attachListener)
        if (ownerView.isAttachedToWindow) addPreDrawListener()
    }

    private var viewTreeObserver: ViewTreeObserver? = null

    private fun addPreDrawListener() {
        viewTreeObserver = ownerView.viewTreeObserver.also { observer ->
            observer.addOnPreDrawListener(preDrawListener)
        }
        tracker.initialize()
    }

    private fun removePreDrawListener() {
        val observer = viewTreeObserver ?: return
        if (observer.isAlive) observer.removeOnPreDrawListener(preDrawListener)
        viewTreeObserver = null
    }

    fun setSize(bounds: Rect) {
        layer.setSize(bounds.width(), bounds.height())
    }

    fun draw(canvas: Canvas) {
        layer.draw(canvas)
    }

    fun refresh() {
        layer.refresh()
    }

    fun dispose() {
        ownerView.removeOnAttachStateChangeListener(attachListener)
        removePreDrawListener()
        layer.dispose()
    }
}