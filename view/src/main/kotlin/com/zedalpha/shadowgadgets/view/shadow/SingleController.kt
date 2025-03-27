package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.isNotDefault
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

internal abstract class SingleController(
    ownerView: View?,
    layerSizeView: View?
) : ShadowController(ownerView, layerSizeView) {

    protected var coreLayer: Layer? = null

    @CallSuper
    override fun checkInvalidate() {
        if (shouldInvalidate()) {
            invalidate()
        } else if (!RenderNodeFactory.isOpen) {
            coreLayer?.refresh()
        }
    }

    protected abstract fun shouldInvalidate(): Boolean

    @CallSuper
    override fun onCreateLayer(layer: Layer) {
        coreLayer = layer
    }

    @CallSuper
    override fun onDisposeLayer(layer: Layer) {
        coreLayer = null
    }

    @CallSuper
    override fun hasColorLayer(): Boolean =
        coreLayer?.color?.isNotDefault == true

    @CallSuper
    override fun recreateColorLayers() {
        coreLayer?.recreate()
    }
}