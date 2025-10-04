package com.zedalpha.shadowgadgets.view.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R

internal val View.viewPainter: ViewPainter?
    get() = (this.rootView as? ViewGroup)
        ?.let { root -> root.mViewPainter ?: ViewPainter(root) }

private var ViewGroup.mViewPainter: ViewPainter?
        by viewTag(R.id.view_painter, null)

internal class ViewPainter(private val owner: ViewGroup) {

    private val painter = Painter(owner.context)

    init {
        owner.mViewPainter = this
        owner.overlay.add(painter)
    }

    private fun dispose() {
        owner.mViewPainter = null
        owner.overlay.remove(painter)
    }

    fun add(view: View) {
        painter.add(view)
    }

    fun remove(view: View) {
        painter.run { remove(view); if (superChildCount == 0) dispose() }
    }

    fun drawView(canvas: Canvas, view: View) {
        if (view.parent === painter) painter.drawView(canvas, view)
    }
}

private class Painter(context: Context) : ViewGroup(context) {

    fun add(view: View) {
        addViewInLayout(view, -1, EmptyLayoutParams, true)
    }

    fun remove(view: View) = removeViewInLayout(view)

    fun drawView(canvas: Canvas, view: View) {
        drawChild(canvas, view, drawingTime)
    }


    init {
        visibility = GONE
        clipChildren = false
        clipToPadding = false
    }

    val superChildCount: Int get() = super.childCount

    @Deprecated("DO NOT USE!")
    override fun getChildCount(): Int = 0


    @Deprecated("DO NOT USE!")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        setMeasuredDimension(0, 0)


    @Deprecated("DO NOT USE!")
    override fun forceLayout() = Unit

    @Deprecated("DO NOT USE!")
    @SuppressLint("MissingSuperCall")
    override fun requestLayout() = Unit

    @Deprecated("DO NOT USE!")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) =
        Unit


    @Suppress("unused")
    private fun dispatchGetDisplayList() = Unit


    @Deprecated("DO NOT USE!")
    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) = Unit

    override fun dispatchDraw(canvas: Canvas) {}


    fun superInvalidate() = super.invalidate()

    @Deprecated("DO NOT USE!")
    override fun invalidate() = Unit

    @Deprecated("DO NOT USE!")
    override fun invalidate(dirty: Rect?) = Unit

    @Deprecated("DO NOT USE!")
    override fun invalidate(l: Int, t: Int, r: Int, b: Int) = Unit

    @Deprecated("DO NOT USE!")
    @SuppressLint("MissingSuperCall")
    override fun onDescendantInvalidated(child: View, target: View) = Unit

    @Suppress("unused")
    private fun damageInParent() = Unit


    @Deprecated("DO NOT USE!")
    override fun invalidateDrawable(drawable: Drawable) = Unit

    @Deprecated("DO NOT USE!")
    override fun invalidateOutline() = Unit


    @Deprecated("DO NOT USE!")
    override fun hasFocus(): Boolean = false

    @Deprecated("DO NOT USE!")
    override fun hasFocusable(): Boolean = false

    @Deprecated("DO NOT USE!")
    override fun hasExplicitFocusable(): Boolean = false
}