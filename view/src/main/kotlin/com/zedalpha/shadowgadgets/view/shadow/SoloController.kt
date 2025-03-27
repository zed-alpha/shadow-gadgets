package com.zedalpha.shadowgadgets.view.shadow

import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.os.Build
import android.util.Size
import android.view.View
import android.view.WindowManager
import com.zedalpha.shadowgadgets.core.layer.Layer

internal class SoloController(
    ownerView: View,
    shadowScope: View?
) : SingleController(ownerView, ownerView.scopeView) {

    val shadow by lazy { SoloShadow(ownerView, this, shadowScope) }

    override fun shouldInvalidate(): Boolean = shadow.shouldInvalidate()

    override fun invalidate() = shadow.invalidate()

    var layerSize: Size = getScreenSize()
        private set

    override fun onCreateLayer(layer: Layer) {
        super.onCreateLayer(layer)
        val scope = scopeView
        val size = if (scope != null) {
            Size(scope.width, scope.height)
        } else {
            getScreenSize()
        }
        layerSize = size
        layer.setSize(size.width, size.height)
    }

    private fun getScreenSize() = ownerView!!.context.getScreenSize()

    override fun onLayerSizeChanged(width: Int, height: Int) {
        if (scopeView != null) coreLayer?.setSize(width, height)
    }
}

internal fun Context.getScreenSize(): Size {
    val manager = getSystemService(WINDOW_SERVICE) as WindowManager
    return if (Build.VERSION.SDK_INT >= 30) {
        val bounds = manager.currentWindowMetrics.bounds
        Size(bounds.width(), bounds.height())
    } else {
        val point = Point()
        @Suppress("DEPRECATION")
        manager.defaultDisplay.getSize(point)
        Size(point.x, point.y)
    }
}