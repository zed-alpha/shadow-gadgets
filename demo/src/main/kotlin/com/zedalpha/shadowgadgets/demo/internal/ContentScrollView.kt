package com.zedalpha.shadowgadgets.demo.internal

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.ScrollView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.withTranslation
import com.zedalpha.shadowgadgets.demo.R

internal class ContentScrollView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ScrollView(context, attrs, android.R.attr.scrollViewStyle) {

    private val drawable =
        AppCompatResources.getDrawable(context, R.drawable.fg_content_border)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        drawable?.setBounds(0, 0, w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.withTranslation(0F, scrollY.toFloat()) { drawable?.draw(canvas) }
    }
}