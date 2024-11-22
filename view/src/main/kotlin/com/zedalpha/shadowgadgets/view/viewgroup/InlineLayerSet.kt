package com.zedalpha.shadowgadgets.view.viewgroup

import android.graphics.Canvas
import android.view.View
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.layer.SingleDrawLayer
import com.zedalpha.shadowgadgets.view.shadow.GroupShadow

internal class InlineLayerSet(private val ownerView: View) {

    private val layersByShadow = mutableMapOf<GroupShadow, SingleDrawLayer?>()

    fun requiresTracking() = layersByShadow.values.any { it != null }

    fun addShadow(shadow: GroupShadow, color: Int) {
        val needsLayer = color != DefaultShadowColorInt || shadow.forceLayer
        layersByShadow[shadow] = if (needsLayer) {
            createLayer(shadow, color)
        } else {
            null
        }
    }

    fun updateColor(shadow: GroupShadow, color: Int) {
        val currentLayer = layersByShadow[shadow]
        val needsLayer = color != DefaultShadowColorInt || shadow.forceLayer
        layersByShadow[shadow] = if (needsLayer) {
            currentLayer?.apply { this.color = color }
                ?: createLayer(shadow, color)
        } else {
            currentLayer?.dispose()
            null
        }
    }

    fun removeShadow(shadow: GroupShadow) {
        layersByShadow.remove(shadow)?.dispose()
    }

    private fun createLayer(shadow: GroupShadow, color: Int) = with(ownerView) {
        SingleDrawLayer(this, color, width, height, shadow::draw)
    }

    fun draw(canvas: Canvas, shadow: GroupShadow) =
        layersByShadow[shadow]?.draw(canvas) ?: shadow.draw(canvas)

    fun setSize(width: Int, height: Int) =
        layersByShadow.values.forEach { it?.setSize(width, height) }

    fun refresh() = layersByShadow.values.forEach { it?.refresh() }

    fun recreate() = layersByShadow.values.forEach { it?.recreate() }

    fun dispose() = layersByShadow.values.forEach { it?.dispose() }
}