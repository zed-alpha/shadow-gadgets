package com.zedalpha.shadowgadgets.core.shadow

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.core.R
import java.util.WeakHashMap


internal class ViewPainterProxy(private val ownerView: View) {

    private var viewPainter: ViewPainter? = null

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            attachToRoot()
        }

        override fun onViewDetachedFromWindow(v: View) {}
    }

    init {
        ownerView.addOnAttachStateChangeListener(attachListener)
        if (ownerView.isAttachedToWindow) attachToRoot()
    }

    private fun attachToRoot() {
        val root = ownerView.rootView as? ViewGroup ?: return
        val painter = root.viewPainter ?: ViewPainter(root)
        painter.registerProxy(this)
        viewPainter = painter
    }

    fun dispose() {
        ownerView.removeOnAttachStateChangeListener(attachListener)
        viewPainter?.unregisterProxy(this)
        viewPainter = null
    }

    fun drawView(canvas: Canvas, view: View) {
        viewPainter?.drawView(canvas, view)
    }
}

@SuppressLint("ViewConstructor")
private class ViewPainter(
    private val ownerView: ViewGroup
) : ViewGroup(ownerView.context) {

    private val uiThread = ownerView.context.mainLooper.thread

    private fun runOnUiThread(block: () -> Unit) {
        if (Thread.currentThread() != uiThread) {
            ownerView.post(block)
        } else {
            block.invoke()
        }
    }

    init {
        visibility = GONE
        ownerView.viewPainter = this
        runOnUiThread { ownerView.overlay.add(this) }
    }

    private fun detachFromOwner() {
        ownerView.viewPainter = null
        runOnUiThread { ownerView.overlay.remove(this) }
    }

    private val activeProxies = WeakHashMap<ViewPainterProxy, Unit>()

    fun registerProxy(proxy: ViewPainterProxy) {
        activeProxies[proxy] = Unit
    }

    fun unregisterProxy(proxy: ViewPainterProxy) {
        activeProxies -= proxy
        if (activeProxies.isEmpty()) detachFromOwner()
    }

    fun drawView(canvas: Canvas, view: View) {
        addViewInLayout(view, 0, emptyLayoutParams, true)
        draw(canvas)
        removeViewInLayout(view)
    }

    override fun onLayout(c: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}

private var ViewGroup.viewPainter: ViewPainter?
    get() = getTag(R.id.view_painter) as? ViewPainter
    set(value) = setTag(R.id.view_painter, value)

private val emptyLayoutParams = ViewGroup.LayoutParams(0, 0)