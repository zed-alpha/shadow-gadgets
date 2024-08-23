package com.zedalpha.shadowgadgets.demo.internal

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import com.google.android.material.card.MaterialCardView
import kotlin.math.roundToInt

class ContentCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : MaterialCardView(context, attrs) {

    private val drawable = SlantGridDrawable()

    init {
        setCardBackgroundColor(Color.WHITE)
        setStrokeColor(ColorStateList.valueOf(Color.LTGRAY))
        strokeWidth = resources.displayMetrics.density.roundToInt()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        drawable.setBounds(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        drawable.draw(canvas)
        super.onDraw(canvas)
    }
}