package com.zedalpha.shadowgadgets.core.shadow

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.core.R
import java.util.WeakHashMap

internal class ViewPainterProxy(
    private val ownerView: View,
    private var initialLayerView: View? = null
) {
    private var viewPainter: ViewPainter? = null

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = attachToRoot()
        override fun onViewDetachedFromWindow(v: View) {}
    }

    init {
        if (ownerView.isAttachedToWindow) {
            attachToRoot()
        } else {
            ownerView.addOnAttachStateChangeListener(attachListener)
        }
    }

    private fun attachToRoot() {
        val root = ownerView.rootView as? ViewGroup ?: return
        val painter = root.viewPainter ?: ViewPainter(root)
        painter.registerProxy(this)
        viewPainter = painter
        initialLayerView?.let { layerView ->
            painter.addLayerView(layerView)
            initialLayerView = null
        }
    }

    fun dispose() {
        ownerView.removeOnAttachStateChangeListener(attachListener)
        viewPainter?.let { painter ->
            painter.unregisterProxy(this)
            viewPainter = null
        }
    }

    fun drawShadowView(canvas: Canvas, shadowView: View) {
        viewPainter?.drawShadowView(canvas, shadowView)
    }

    fun drawLayerView(canvas: Canvas, layerView: View) {
        viewPainter?.drawLayerView(canvas, layerView)
    }

    fun replaceLayerView(layerView: View, newLayerView: View) {
        viewPainter?.apply {
            removeLayerView(layerView)
            addLayerView(newLayerView)
        }
    }

    fun invalidateLayerView(layerView: View) {
        viewPainter?.invalidateLayerView(layerView)
    }

    fun refreshLayerView(layerView: View) {
        viewPainter?.refreshLayerView(layerView)
    }
}

@SuppressLint("ViewConstructor")
internal class ViewPainter(private val ownerView: ViewGroup) :
    ViewGroup(ownerView.context) {

    init {
        visibility = GONE
        ownerView.viewPainter = this
        ownerView.overlay.add(this)
    }

    private fun detachFromOwner() {
        ownerView.viewPainter = null
        ownerView.overlay.remove(this)
    }

    private val activeProxies = WeakHashMap<ViewPainterProxy, Unit>()

    fun registerProxy(proxy: ViewPainterProxy) {
        activeProxies[proxy] = Unit
    }

    fun unregisterProxy(proxy: ViewPainterProxy) {
        activeProxies -= proxy
        if (activeProxies.isEmpty()) detachFromOwner()
    }

    fun drawShadowView(canvas: Canvas, shadowView: View) {
        addViewInLayout(shadowView, -1, EmptyLayoutParams, true)
        draw(canvas)
        removeViewInLayout(shadowView)
    }

    fun addLayerView(layerView: View) = addView(layerView, EmptyLayoutParams)

    fun removeLayerView(layerView: View) = removeView(layerView)

    fun drawLayerView(canvas: Canvas, layerView: View) {
        if (indexOfChild(layerView) < 0) return
        drawChild(canvas, layerView, 0L)
    }

    fun refreshLayerView(layerView: View) {
        if (indexOfChild(layerView) < 0) return
        detachViewFromParent(layerView)
        attachViewToParent(layerView, -1, EmptyLayoutParams)
    }

    fun invalidateLayerView(layerView: View) {
        layerView.invalidate()
        invalidate()
    }

    override fun forceLayout() {}

    @SuppressLint("MissingSuperCall") override fun requestLayout() {}

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}

private var ViewGroup.viewPainter: ViewPainter?
    get() = getTag(R.id.view_painter) as? ViewPainter
    set(value) = setTag(R.id.view_painter, value)

private val EmptyLayoutParams = ViewGroup.LayoutParams(0, 0)