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
    get() {
        val root = this.rootView as? ViewGroup ?: return null
        return root.mViewPainter ?: ViewPainter(root)
    }

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

    fun add(view: View) = painter.add(view)

    fun remove(view: View) =
        painter.let { it.remove(view); if (it.isEmpty()) dispose() }

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

    fun isEmpty(): Boolean = super.childCount == 0

    @Deprecated("Library stop")
    override fun getChildCount(): Int = 0

    @Deprecated("Library stop")
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) =
        setMeasuredDimension(0, 0)

    @Deprecated("Library stop")
    override fun forceLayout() = Unit

    @Deprecated("Library stop")
    @SuppressLint("MissingSuperCall")
    override fun requestLayout() = Unit

    @Deprecated("Library stop")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) =
        Unit

    @Deprecated("Library stop")
    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) = Unit

    @Deprecated("Library stop")
    override fun dispatchDraw(canvas: Canvas) = Unit

    @Deprecated("Library stop")
    override fun invalidate() = Unit

    @Deprecated("Library stop")
    override fun invalidate(dirty: Rect?) = Unit

    @Deprecated("Library stop")
    override fun invalidate(l: Int, t: Int, r: Int, b: Int) = Unit

    @Deprecated("Library stop")
    @SuppressLint("MissingSuperCall")
    override fun onDescendantInvalidated(child: View, target: View) = Unit

    @Deprecated("Library stop")
    fun damageInParent() = Unit

    @Deprecated("Library stop")
    override fun invalidateDrawable(drawable: Drawable) = Unit

    @Deprecated("Library stop")
    override fun invalidateOutline() = Unit

    @Deprecated("Library stop")
    override fun hasFocus(): Boolean = false

    @Deprecated("Library stop")
    override fun hasFocusable(): Boolean = false

    @Deprecated("Library stop")
    override fun hasExplicitFocusable(): Boolean = false
}