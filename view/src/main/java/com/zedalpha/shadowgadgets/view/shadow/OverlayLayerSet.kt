package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.layer.LayerDraw


internal class OverlayLayerSet(private val ownerView: ViewGroup) {

    private val activeLayers = mutableMapOf<Int, Layer>()

    private val drawLayers = mutableMapOf<LayerDraw, Layer?>()

    fun requiresTracking() = activeLayers.isNotEmpty()

    fun addShadow(shadow: GroupShadow, color: Int) {
        val needsLayer = color != DefaultShadowColorInt || shadow.forceLayer
        drawLayers[shadow] = if (needsLayer) {
            obtainLayer(shadow, color, null)
        } else {
            null
        }
    }

    fun updateColor(shadow: GroupShadow, color: Int) {
        val current = drawLayers.remove(shadow)?.also { it.removeDraw(shadow) }
        val recycled = current?.let { layer ->
            if (layer.isEmpty()) {
                activeLayers.remove(layer.color)
                layer
            } else {
                null
            }
        }
        val needsLayer = color != DefaultShadowColorInt || shadow.forceLayer
        drawLayers[shadow] = if (needsLayer) {
            obtainLayer(shadow, color, recycled)
        } else {
            recycled?.dispose()
            null
        }
    }

    fun removeShadow(shadow: GroupShadow) {
        drawLayers.remove(shadow)?.run {
            removeDraw(shadow)
            if (isEmpty()) {
                activeLayers.remove(color)
                dispose()
            }
        }
    }

    private fun obtainLayer(
        shadow: GroupShadow,
        color: Int,
        recycled: Layer?
    ): Layer {
        val layer = activeLayers[color]?.also { recycled?.dispose() }
            ?: recycled?.also { it.color = color; activeLayers[color] = it }
            ?: with(ownerView) { Layer(this, color, width, height) }
                .also { activeLayers[color] = it }
        return layer.apply { addDraw(shadow) }
    }

    fun draw(canvas: Canvas) {
        activeLayers.values.forEach { it.draw(canvas) }
        drawLayers.entries.forEach { (shadow, layer) ->
            if (layer == null) shadow.draw(canvas)
        }
    }

    fun setSize(width: Int, height: Int) {
        activeLayers.values.forEach { it.setSize(width, height) }
    }

    fun invalidate() {
        activeLayers.values.forEach { it.invalidate() }
    }

    fun refresh() {
        activeLayers.values.forEach { it.refresh() }
    }

    fun recreate() {
        activeLayers.values.forEach { it.recreate() }
    }

    fun dispose() {
        activeLayers.values.forEach { it.dispose() }
    }
}