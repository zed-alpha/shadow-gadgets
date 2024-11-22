package com.zedalpha.shadowgadgets.view.drawable

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewTreeObserver
import com.zedalpha.shadowgadgets.core.layer.LayerDraw
import com.zedalpha.shadowgadgets.core.layer.LocationTracker
import com.zedalpha.shadowgadgets.core.layer.SingleDrawLayer

internal class SoloLayer(
    private val drawable: Drawable,
    private val ownerView: View,
    layerDraw: LayerDraw,
    color: Int
) {
    private val coreLayer =
        SingleDrawLayer(
            ownerView,
            color,
            drawable.bounds.width(),
            drawable.bounds.height(),
            layerDraw
        )

    private val tracker = LocationTracker(ownerView)

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = addPreDrawListener()
        override fun onViewDetachedFromWindow(v: View) = removePreDrawListener()
    }

    private val preDrawListener = ViewTreeObserver.OnPreDrawListener {
        if (tracker.checkLocationChanged()) {
            coreLayer.recreate()
            drawable.invalidateSelf()
        }
        true
    }

    var color by coreLayer::color

    init {
        ownerView.addOnAttachStateChangeListener(attachListener)
        if (ownerView.isAttachedToWindow) addPreDrawListener()
    }

    private var viewTreeObserver: ViewTreeObserver? = null

    private fun addPreDrawListener() {
        viewTreeObserver = ownerView.viewTreeObserver.apply {
            if (isAlive) addOnPreDrawListener(preDrawListener)
        }
        tracker.initialize()
    }

    private fun removePreDrawListener() {
        val observer = viewTreeObserver ?: return
        if (observer.isAlive) observer.removeOnPreDrawListener(preDrawListener)
        viewTreeObserver = null
    }

    fun setSize(bounds: Rect) =
        coreLayer.setSize(bounds.width(), bounds.height())

    fun draw(canvas: Canvas) = coreLayer.draw(canvas)

    fun refresh() = coreLayer.refresh()

    fun dispose() {
        ownerView.removeOnAttachStateChangeListener(attachListener)
        removePreDrawListener()
        coreLayer.dispose()
    }
}