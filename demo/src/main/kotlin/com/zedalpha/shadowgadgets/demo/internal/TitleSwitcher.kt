package com.zedalpha.shadowgadgets.demo.internal

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.AnimationUtils
import android.widget.TextSwitcher
import com.zedalpha.shadowgadgets.demo.R

class TitleSwitcher @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : TextSwitcher(context, attributeSet) {

    private val forward = Pair(
        AnimationUtils.loadAnimation(context, R.anim.slide_in_top),
        AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom)
    )

    private val backward = Pair(
        AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom),
        AnimationUtils.loadAnimation(context, R.anim.slide_out_top)
    )

    init {
        val inflater = LayoutInflater.from(context)
        setFactory {
            inflater.inflate(
                R.layout.internal_title_switcher,
                this,
                false
            )
        }
        setDirection(true)
    }

    fun setDirection(goForward: Boolean) {
        if (goForward) {
            inAnimation = forward.first; outAnimation = forward.second
        } else {
            inAnimation = backward.first; outAnimation = backward.second
        }
    }
}