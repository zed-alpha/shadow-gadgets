package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider

internal class NullShadow(targetView: View) : ViewShadow(targetView, null) {

    init {
        targetView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                provider.getOutline(view, outline)
                outline.alpha = 0F
            }
        }
    }

    override fun invalidate() {}
}