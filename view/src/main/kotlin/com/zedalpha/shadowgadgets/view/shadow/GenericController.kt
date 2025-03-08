package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import com.zedalpha.shadowgadgets.core.isNotDefault
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

internal abstract class GenericController<T>(ownerView: View?) :
    ShadowController(ownerView) {

    abstract val shadow: T

    override fun checkInvalidate() {
        if (shouldInvalidate()) {
            invalidate()
        } else if (!RenderNodeFactory.isOpen) {
            colorLayer?.refresh()
        }
    }

    protected abstract fun shouldInvalidate(): Boolean

    private var colorLayer: Layer? = null

    override fun onCreateLayer(layer: Layer) {
        if (layer.color.isNotDefault) colorLayer = layer
    }

    override fun onDisposeLayer(layer: Layer) {
        colorLayer = null
    }

    override fun hasColorLayer(): Boolean = colorLayer != null

    override fun recreateColorLayers() {
        colorLayer?.recreate()
    }

    override fun onSizeChanged(width: Int, height: Int) {
        colorLayer?.setSize(width, height)
    }
}