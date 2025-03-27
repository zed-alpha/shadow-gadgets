package com.zedalpha.shadowgadgets.view.drawable

import android.view.View
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.view.shadow.SingleController

internal class DrawableController(
    private val drawable: ShadowDrawable,
    ownerView: View?
) : SingleController(ownerView, null) {

    override fun shouldInvalidate(): Boolean = false

    override fun invalidate() = drawable.invalidateSelf()

    override fun onCreateLayer(layer: Layer) {
        super.onCreateLayer(layer)
        val bounds = drawable.bounds
        layer.setSize(bounds.width(), bounds.height())
    }
}