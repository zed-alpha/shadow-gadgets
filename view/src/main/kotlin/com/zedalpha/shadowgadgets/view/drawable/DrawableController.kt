package com.zedalpha.shadowgadgets.view.drawable

import android.view.View
import com.zedalpha.shadowgadgets.view.shadow.GenericController

internal class DrawableController(
    override val shadow: ShadowDrawable,
    ownerView: View?
) : GenericController<ShadowDrawable>(ownerView) {

    override fun shouldInvalidate(): Boolean = false

    override fun invalidate() = shadow.invalidateSelf()
}