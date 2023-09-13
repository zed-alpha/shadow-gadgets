package com.zedalpha.shadowgadgets.core.layer

import android.graphics.Canvas
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup


internal class ViewLayer(
    private val ownerView: View,
    override val drawContent: (Canvas) -> Unit
) : ManagedLayer {

    private val layerView = LayerView()

    override fun recreate() {
        layerView.recreate()
    }

    override fun dispose() {
        layerView.dispose()
    }

    override fun setSize(width: Int, height: Int) {
        layerView.setSize(width, height)
    }

    override fun setLayerPaint(paint: Paint) {
        layerView.setInnerLayerPaint(paint)
    }

    override fun draw(canvas: Canvas) {
        layerView.draw(canvas)
    }

    override fun invalidate() {
        layerView.invalidate()
    }

    override fun refresh() {
        layerView.refresh()
    }

    inner class LayerView : ViewGroup(ownerView.context) {

        private var innerView = createInnerView()

        fun recreate() {
            removeView(innerView)
            innerView = createInnerView()
        }

        private fun createInnerView() = object : View(context) {
            override fun onDraw(canvas: Canvas) {
                drawContent(canvas)
            }
        }.also { view ->
            view.setLayerType(LAYER_TYPE_HARDWARE, null)
            addView(view, emptyLayoutParams)
        }

        private val attachListener = object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                addToOverlay()
            }

            override fun onViewDetachedFromWindow(v: View) {
                removeFromOverlay()
            }
        }

        init {
            visibility = GONE
            ownerView.addOnAttachStateChangeListener(attachListener)
            if (ownerView.isAttachedToWindow) addToOverlay()
        }

        fun dispose() {
            ownerView.removeOnAttachStateChangeListener(attachListener)
            removeFromOverlay()
        }

        private var rootView: ViewGroup? = null

        private fun addToOverlay() {
            val root = ownerView.rootView as? ViewGroup ?: return
            rootView = root
            root.overlay.add(this)
        }

        private fun removeFromOverlay() {
            rootView?.overlay?.remove(this)
        }

        fun setSize(width: Int, height: Int) {
            layout(0, 0, width, height)
            innerView.layout(0, 0, width, height)
        }

        fun setInnerLayerPaint(paint: Paint) {
            innerView.setLayerPaint(paint)
        }

        override fun invalidate() {
            super.invalidate()
            innerView.invalidate()
        }

        fun refresh() {
            detachViewFromParent(innerView)
            attachViewToParent(innerView, 0, emptyLayoutParams)
        }

        override fun onLayout(c: Boolean, l: Int, t: Int, r: Int, b: Int) {}
    }
}

private val emptyLayoutParams = ViewGroup.LayoutParams(0, 0)