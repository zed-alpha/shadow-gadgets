package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Canvas
import android.view.ViewGroup
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.layer.MultiDrawLayer

internal class OverlayLayerSet(private val ownerView: ViewGroup) {

    private val layersByColor = mutableMapOf<Int, MultiDrawLayer>()

    private val layersByShadow = mutableMapOf<GroupShadow, MultiDrawLayer?>()

    fun requiresTracking() = layersByColor.isNotEmpty()

    fun addShadow(shadow: GroupShadow, color: Int) {
        val needsLayer = color != DefaultShadowColorInt || shadow.forceLayer
        layersByShadow[shadow] = if (needsLayer) {
            obtainLayer(shadow, color, null)
        } else {
            null
        }
    }

    fun updateColor(shadow: GroupShadow, color: Int) {
        val recycled = layersByShadow.remove(shadow)?.let { layer ->
            layer.removeDraw(shadow)
            if (layer.isEmpty()) {
                layersByColor.remove(layer.color)
                layer
            } else {
                null
            }
        }
        val needsLayer = color != DefaultShadowColorInt || shadow.forceLayer
        layersByShadow[shadow] = if (needsLayer) {
            obtainLayer(shadow, color, recycled)
        } else {
            recycled?.dispose()
            null
        }
    }

    fun removeShadow(shadow: GroupShadow) {
        layersByShadow.remove(shadow)?.run {
            removeDraw(shadow)
            if (isEmpty()) {
                layersByColor.remove(color)
                dispose()
            }
        }
    }

    private fun obtainLayer(
        shadow: GroupShadow,
        color: Int,
        recycled: MultiDrawLayer?
    ): MultiDrawLayer {
        val layer = layersByColor[color]?.also { recycled?.dispose() }
            ?: recycled?.also { it.color = color; layersByColor[color] = it }
            ?: with(ownerView) { MultiDrawLayer(this, color, width, height) }
                .also { layersByColor[color] = it }
        return layer.apply { addDraw(shadow) }
    }

    fun draw(canvas: Canvas) {
        layersByShadow.entries.forEach { (layerDraw, layer) ->
            if (layer == null) layerDraw.draw(canvas)
        }
        layersByColor.values.forEach { layer -> layer.draw(canvas) }
    }

    fun setSize(width: Int, height: Int) =
        layersByColor.values.forEach { it.setSize(width, height) }

    fun invalidate() = layersByColor.values.forEach { it.invalidate() }

    fun refresh() = layersByColor.values.forEach { it.refresh() }

    fun recreate() = layersByColor.values.forEach { it.recreate() }

    fun dispose() = layersByColor.values.forEach { it.dispose() }
}