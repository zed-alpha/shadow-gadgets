package com.zedalpha.shadowgadgets.view.viewgroup

import android.graphics.Canvas
import android.view.View
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.layer.LayerDraw
import com.zedalpha.shadowgadgets.view.shadow.GroupShadow


internal class InlineLayerSet(private val ownerView: View) {

    private val drawLayers = mutableMapOf<LayerDraw, Layer?>()

    fun requiresTracking() = drawLayers.values.any { it != null }

    fun addShadow(shadow: GroupShadow, color: Int) {
        val needsLayer = color != DefaultShadowColorInt || shadow.forceLayer
        drawLayers[shadow] = if (needsLayer) {
            createLayer(shadow, color)
        } else {
            null
        }
    }

    fun updateColor(shadow: GroupShadow, color: Int) {
        val currentLayer = drawLayers[shadow]
        val needsLayer = color != DefaultShadowColorInt || shadow.forceLayer
        drawLayers[shadow] = if (needsLayer) {
            currentLayer?.apply { this.color = color }
                ?: createLayer(shadow, color)
        } else {
            currentLayer?.dispose()
            null
        }
    }

    fun removeShadow(shadow: GroupShadow) {
        drawLayers.remove(shadow)?.dispose()
    }

    private fun createLayer(shadow: GroupShadow, color: Int) = with(ownerView) {
        Layer(this, color, width, height).apply { addDraw(shadow) }
    }

    fun draw(canvas: Canvas, shadow: GroupShadow) {
        drawLayers[shadow]?.draw(canvas) ?: shadow.draw(canvas)
    }

    fun setSize(width: Int, height: Int) {
        drawLayers.values.forEach { it?.setSize(width, height) }
    }

    fun refresh() {
        drawLayers.values.forEach { it?.refresh() }
    }

    fun recreate() {
        drawLayers.values.forEach { it?.recreate() }
    }

    fun dispose() {
        drawLayers.values.forEach { it?.dispose() }
    }
}