package com.zedalpha.shadowgadgets.demo.internal

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.content.res.AppCompatResources
import com.zedalpha.shadowgadgets.demo.R

class ContentCardView
@JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    init {
        background = SlantGridDrawable()
        foreground =
            AppCompatResources.getDrawable(context, R.drawable.fg_frame)
        outlineProvider = RoundedCornerViewOutlineProvider()
        clipToOutline = true
    }
}