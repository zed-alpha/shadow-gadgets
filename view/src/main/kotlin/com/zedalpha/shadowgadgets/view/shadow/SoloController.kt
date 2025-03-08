package com.zedalpha.shadowgadgets.view.shadow

import android.view.View

internal class SoloController(ownerView: View, shadowScope: View?) :
    GenericController<SoloShadow>(ownerView) {

    override val shadow: SoloShadow = SoloShadow(ownerView, this, shadowScope)

    override fun shouldInvalidate(): Boolean = shadow.shouldInvalidate()

    // Solos invalidate automatically along with the target.
    override fun invalidate() {}
}