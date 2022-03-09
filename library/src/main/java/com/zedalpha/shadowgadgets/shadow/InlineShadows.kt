@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi

internal class ViewInlineShadow(
    targetView: View,
    private val container: ViewShadowContainer
) : ViewShadow(targetView) {
    override fun updateShadow(): Boolean {
        val view = shadowView
        val index = container.detachShadowView(view)
        val result = super.updateShadow()
        container.reAttachShadowView(view, index)
        return result
    }
}