package com.zedalpha.shadowgadgets.view.plane

import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.internal.Group
import com.zedalpha.shadowgadgets.view.internal.OnPreDraw
import com.zedalpha.shadowgadgets.view.internal.SwitchGroup
import com.zedalpha.shadowgadgets.view.internal.addOnPreDraw
import com.zedalpha.shadowgadgets.view.internal.invalidator
import com.zedalpha.shadowgadgets.view.internal.removeOnPreDraw
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.layer.AutoPositionLayer
import com.zedalpha.shadowgadgets.view.layer.Layer
import com.zedalpha.shadowgadgets.view.layer.LayerGroup
import com.zedalpha.shadowgadgets.view.layer.desiredLayerColor
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup

internal fun <T> T.getOrCreateInlinePlane(): Plane
        where T : ViewGroup, T : ShadowsViewGroup =
    this.inlinePlane ?: InlineGroupPlane(this)

internal var <T> T.inlinePlane: Plane?
        where T : ViewGroup, T : ShadowsViewGroup
        by viewTag(R.id.inline_plane, null)
    private set

internal class InlineGroupPlane<T>(override val viewGroup: T) :
    GroupPlane(viewGroup) where T : ViewGroup, T : ShadowsViewGroup {

    private var proxies: Group<ShadowProxy>? = null

    private var layers: LayerGroup<Layer>? = null

    private val invalidator = viewGroup.invalidator()

    private val checkInvalidate =
        OnPreDraw {
            if (invalidator.isInvalidated) return@OnPreDraw
            if (proxies?.isInvalid() == true) invalidate()
        }

    init {
        viewGroup.inlinePlane = this
        viewGroup.addOnPreDraw(checkInvalidate)
        invalidator.add(this)
    }

    private fun dispose() {
        viewGroup.inlinePlane = null
        viewGroup.removeOnPreDraw(checkInvalidate)
        invalidator.remove(this)
    }

    override fun addProxy(proxy: ShadowProxy) {
        updateLayer(proxy)
        val proxies = this.proxies
            ?: SwitchGroup<ShadowProxy>().also { this.proxies = it }
        proxies.add(proxy)
    }

    override fun updateLayer(proxy: ShadowProxy) {
        val current = proxy.layer
        val color = proxy.desiredLayerColor
        if (current?.color == color) return

        if (color == null) {
            disposeLayer(proxy)
        } else {
            val layer = current ?: createLayer(proxy).also { proxy.layer = it }
            layer.color = color
        }

        layers?.updateRecreateCount()
    }

    private fun createLayer(proxy: ShadowProxy): Layer {
        val layers = this.layers
            ?: LayerGroup<Layer>(viewGroup, this)
                .also { this.layers = it }

        val layer = AutoPositionLayer(viewGroup, proxy::updateAndDraw)
        layers.add(layer)
        return layer
    }

    override fun removeProxy(proxy: ShadowProxy) {
        val proxies = checkNotNull(this.proxies) { "GroupPlane is empty" }

        disposeLayer(proxy)
        layers?.updateRecreateCount()

        proxies.remove(proxy)
        if (proxies.isEmpty) dispose()
    }

    private fun disposeLayer(proxy: ShadowProxy) {
        val layer = proxy.layer ?: return
        val layers = checkNotNull(this.layers) { "LayerGroup is empty" }

        proxy.layer = null
        layer.dispose()
        layers.remove(layer)
        if (layers.isEmpty) this.layers = null
    }

    override fun invalidate() = invalidator.invalidate()
}