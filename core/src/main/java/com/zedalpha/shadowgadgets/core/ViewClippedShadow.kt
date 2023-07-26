package com.zedalpha.shadowgadgets.core

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import java.util.WeakHashMap


class ViewClippedShadow(ownerView: View) : ClippedShadow() {

    private val shadowView = View(ownerView.context).apply {
        // Ensures draw when target is partially/fully out of bounds
        right = Int.MAX_VALUE; bottom = Int.MAX_VALUE
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.set(this@ViewClippedShadow.shadowOutline)
            }
        }
    }

    private var viewPainter: ViewPainter? = null

    init {
        val rootView = ownerView.rootView as? ViewGroup
        if (rootView != null) {
            val painter = ViewPainter.forOwner(rootView)
            painter.registerView(shadowView)
            viewPainter = painter
        }
    }

    override fun dispose() {
        super.dispose()
        viewPainter?.unregisterView(shadowView)
        viewPainter = null
    }

    override var alpha: Float
        get() = shadowView.alpha
        set(value) {
            shadowView.alpha = value
        }

    override var cameraDistance: Float
        get() = shadowView.cameraDistance
        set(value) {
            shadowView.cameraDistance = value
        }

    override var elevation: Float
        get() = shadowView.elevation
        set(value) {
            shadowView.elevation = value
        }

    override var pivotX: Float
        get() = shadowView.pivotX
        set(value) {
            shadowView.pivotX = value
        }

    override var pivotY: Float
        get() = shadowView.pivotY
        set(value) {
            shadowView.pivotY = value
        }

    override var rotationX: Float
        get() = shadowView.rotationX
        set(value) {
            shadowView.rotationX = value
        }

    override var rotationY: Float
        get() = shadowView.rotationY
        set(value) {
            shadowView.rotationY = value
        }

    override var rotationZ: Float
        get() = shadowView.rotation
        set(value) {
            shadowView.rotation = value
        }

    override var scaleX: Float
        get() = shadowView.scaleX
        set(value) {
            shadowView.scaleX = value
        }

    override var scaleY: Float
        get() = shadowView.scaleY
        set(value) {
            shadowView.scaleY = value
        }

    override var translationX: Float
        get() = shadowView.translationX
        set(value) {
            shadowView.translationX = value
        }

    override var translationY: Float
        get() = shadowView.translationY
        set(value) {
            shadowView.translationY = value
        }

    override var translationZ: Float
        get() = shadowView.translationZ
        set(value) {
            shadowView.translationZ = value
        }

    override var ambientColor: Int
        get() = when {
            Build.VERSION.SDK_INT < 28 -> DefaultShadowColorInt
            else -> ViewShadowColorsHelper.getAmbientColor(shadowView)
        }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.setAmbientColor(shadowView, value)
            }
        }

    override var spotColor: Int
        get() = when {
            Build.VERSION.SDK_INT < 28 -> DefaultShadowColorInt
            else -> ViewShadowColorsHelper.getSpotColor(shadowView)
        }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.setSpotColor(shadowView, value)
            }
        }

    override fun hasIdentityMatrix(): Boolean =
        shadowView.matrix.isIdentity

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
        fun forOwner(ownerView: ViewGroup) = ownerView.viewPainter
            ?: ViewPainter(ownerView).also { ownerView.viewPainter = it }

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

    private fun detachFromOwner() {
        ownerView.viewPainter = null
        runOnUiThread { ownerView.overlay.remove(this) }
    }

    private val activeViews = WeakHashMap<View, Unit>()

    fun registerView(view: View) {
        activeViews[view] = Unit
    }

    fun unregisterView(view: View) {
        activeViews -= view
        if (activeViews.isEmpty()) detachFromOwner()
    }

    fun drawView(view: View, canvas: Canvas) {
        addViewInLayout(view, 0, EmptyLayoutParams, true)
        draw(canvas)
        removeViewInLayout(view)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {}
}

private val EmptyLayoutParams = ViewGroup.LayoutParams(0, 0)