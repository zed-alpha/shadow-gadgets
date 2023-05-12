package com.zedalpha.shadowgadgets.core

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider


class ViewClippedShadow(private val ownerView: View) : ClippedShadow() {

    private val shadowView = View(ownerView.context).apply {
        right = 1
        bottom = 1
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.set(this@ViewClippedShadow.outline)
            }
        }
    }

    private var viewPainter: ViewPainter? = null

    private val attachListener = object : View.OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) {
            attachToPainter()
        }

        override fun onViewDetachedFromWindow(v: View) {
            detachFromPainter()
        }
    }

    init {
        ownerView.apply {
            addOnAttachStateChangeListener(attachListener)
            if (isAttachedToWindow) attachToPainter()
        }
    }

    override fun dispose() {
        super.dispose()
        ownerView.apply {
            removeOnAttachStateChangeListener(attachListener)
            if (isAttachedToWindow) detachFromPainter()
        }
    }

    private fun attachToPainter() {
        val viewGroup = ownerView.rootView as? ViewGroup ?: return
        val painter = ViewPainter.forView(viewGroup)
        painter.registerView(shadowView)
        viewPainter = painter
    }

    private fun detachFromPainter() {
        viewPainter?.unregisterView(shadowView)
        viewPainter = null
    }

    override var alpha: Float by shadowView::alpha

    override var cameraDistance: Float by shadowView::cameraDistance

    override var elevation: Float by shadowView::elevation

    override var pivotX: Float by shadowView::pivotX

    override var pivotY: Float by shadowView::pivotY

    override var rotationX: Float by shadowView::rotationX

    override var rotationY: Float by shadowView::rotationY

    override var rotationZ: Float by shadowView::rotation

    override var scaleX: Float by shadowView::scaleX

    override var scaleY: Float by shadowView::scaleY

    override var translationX: Float by shadowView::translationX

    override var translationY: Float by shadowView::translationY

    override var translationZ: Float by shadowView::translationZ

    override var ambientColor: Int
        get() = when {
            Build.VERSION.SDK_INT < 28 -> DefaultShadowColorInt
            else -> ViewShadowColors28.getAmbientColor(shadowView)
        }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColors28.setAmbientColor(shadowView, value)
            }
        }

    override var spotColor: Int
        get() = when {
            Build.VERSION.SDK_INT < 28 -> DefaultShadowColorInt
            else -> ViewShadowColors28.getSpotColor(shadowView)
        }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColors28.setSpotColor(shadowView, value)
            }
        }

    override fun hasIdentityMatrix() = shadowView.matrix.isIdentity

    override fun getMatrix(outMatrix: Matrix) {
        outMatrix.set(shadowView.matrix)
    }

    override fun onDraw(canvas: Canvas) {
        viewPainter?.drawView(shadowView, canvas)
    }
}

@SuppressLint("ViewConstructor")
private class ViewPainter private constructor(
    private val ownerView: ViewGroup
) : ViewGroup(ownerView.context) {

    companion object {
        fun forView(viewGroup: ViewGroup) = viewGroup.viewPainter
            ?: ViewPainter(viewGroup).also { viewGroup.viewPainter = it }

        private var ViewGroup.viewPainter: ViewPainter?
            get() = getTag(R.id.view_painter) as? ViewPainter
            set(value) = setTag(R.id.view_painter, value)
    }

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
        runOnUiThread { ownerView.overlay.add(this) }
    }

    private fun detach() {
        val owner = ownerView
        owner.viewPainter = null
        runOnUiThread { owner.overlay.remove(this) }
    }

    private val activeViews = mutableSetOf<View>()

    fun registerView(view: View) {
        activeViews += view
    }

    fun unregisterView(view: View) {
        activeViews.apply { remove(view); if (isEmpty()) detach() }
    }

    fun drawView(view: View, canvas: Canvas) {
        addViewInLayout(view, 0, EmptyLayoutParams, true)
        draw(canvas)
        removeViewInLayout(view)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}

private val EmptyLayoutParams = ViewGroup.LayoutParams(0, 0)