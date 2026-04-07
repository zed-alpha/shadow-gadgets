package com.zedalpha.shadowgadgets.view.internal

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R

internal fun View.obtainViewPainter(): ViewPainter? {
    val root = this.rootView as? ViewGroup ?: return null
    return root.viewPainter ?: ViewPainter(root)
}

private var ViewGroup.viewPainter: ViewPainter?
        by viewTag(R.id.view_painter, null)

internal class ViewPainter(private val owner: ViewGroup) {

    private val painter = Painter(owner.context)

    init {
        owner.viewPainter = this
        owner.overlay.add(painter)
    }

    private fun dispose() {
        owner.viewPainter = null
        owner.overlay.remove(painter)
    }

    fun add(view: View) = painter.add(view)

    fun remove(view: View) {
        val painter = this.painter
        painter.remove(view)
        if (painter.isEmpty()) dispose()
    }

    fun drawView(canvas: Canvas, view: View) {
        if (view.parent === painter) painter.drawView(canvas, view)
    }
}

private class Painter(context: Context) : ViewGroup(context) {

    init {
        visibility = GONE
        clipChildren = false
        clipToPadding = false
    }

    fun add(view: View) {
        addViewInLayout(view, -1, EmptyLayoutParams, true)
    }

    fun remove(view: View) = removeViewInLayout(view)

    fun isEmpty(): Boolean = super.childCount == 0

    fun drawView(canvas: Canvas, view: View) {
        drawChild(canvas, view, drawingTime)
    }

    @Deprecated("Library stop")
    fun damageInParent() = Unit

    @Deprecated("Library stop")
    override fun getChildCount(): Int = 0

    @Deprecated("Library stop")
    override fun hasFocus(): Boolean = false

    @Deprecated("Library stop")
    @SuppressLint("MissingSuperCall")
    override fun onDescendantInvalidated(child: View, target: View) = Unit

    @Deprecated("Library stop")
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    @Deprecated("Library stop")
    @SuppressLint("MissingSuperCall")
    override fun requestLayout() = Unit
}