package com.zedalpha.shadowgadgets.view.plane

import android.graphics.Canvas
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.internal.OnPreDraw
import com.zedalpha.shadowgadgets.view.internal.addOnPreDraw
import com.zedalpha.shadowgadgets.view.internal.invalidator
import com.zedalpha.shadowgadgets.view.internal.removeOnPreDraw
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.layer.GroupLayer
import com.zedalpha.shadowgadgets.view.layer.InertGroupLayer
import com.zedalpha.shadowgadgets.view.layer.LayerGroup
import com.zedalpha.shadowgadgets.view.layer.desiredLayerColor
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import kotlin.reflect.KMutableProperty0

internal fun ViewGroup.getOrCreateForegroundPlane(): Plane =
    this.foregroundPlane
        ?: OverlayPlane(this, ::OverlayDrawable, this::foregroundPlane)

internal var ViewGroup.foregroundPlane: OverlayPlane?
        by viewTag(R.id.foreground_plane, null)

internal fun ViewGroup.getOrCreateBackgroundPlane(): Plane =
    this.backgroundPlane
        ?: OverlayPlane(this, OverlayProjector::new, this::backgroundPlane)

internal var ViewGroup.backgroundPlane: OverlayPlane?
        by viewTag(R.id.background_plane, null)

internal class OverlayPlane(
    viewGroup: ViewGroup,
    createDrawable: (ViewGroup, (Canvas) -> Unit) -> OverlayDrawable,
    private val viewTag: KMutableProperty0<OverlayPlane?>
) : GroupPlane(viewGroup) {

    private var inertLayer: InertGroupLayer? = null

    private var activeLayers: LayerGroup<GroupLayer>? = null

    private val invalidator = viewGroup.invalidator()

    private val checkInvalidate =
        OnPreDraw {
            if (invalidator.isInvalidated) return@OnPreDraw
            val isInvalid =
                inertLayer?.isInvalid() == true ||
                        activeLayers?.has { it.isInvalid() } == true
            if (isInvalid) invalidate()
        }

    private val drawable =
        createDrawable(viewGroup) { canvas ->
            inertLayer?.iterate { it.updateAndDraw(canvas) }
            activeLayers?.iterate { it.draw(canvas) }
        }

    init {
        viewTag.set(this)
        viewGroup.addOnPreDraw(checkInvalidate)
        invalidator.add(this)
    }

    @CallSuper
    private fun dispose() {
        viewTag.set(null)
        viewGroup.removeOnPreDraw(checkInvalidate)
        invalidator.remove(this)
        drawable.dispose()
    }

    override fun invalidate() {
        drawable.invalidateSelf()
        invalidator.invalidate()
    }

    override fun addProxy(proxy: ShadowProxy) = updateLayer(proxy)

    override fun updateLayer(proxy: ShadowProxy) {
        val current = proxy.layer as GroupLayer?
        val color = proxy.desiredLayerColor

        if (current != null &&
            (color == null && current == inertLayer ||
                    color != null && current.color == color)
        ) {
            return
        }

        var recycled: GroupLayer? = null
        if (current != null) {
            proxy.layer = null
            current.remove(proxy)
            if (current.isEmpty) {
                if (current == inertLayer) {
                    disposeInertLayer(current)
                } else {
                    recycled = current
                }
            }
        }

        val next =
            if (color == null) {
                recycled?.let { disposeActiveLayer(it) }
                inertLayer ?: InertGroupLayer().also { inertLayer = it }
            } else {
                val layers = activeLayers
                    ?: LayerGroup<GroupLayer>(viewGroup, this)
                        .also { activeLayers = it }

                val existing = layers.leading { it.color == color }
                if (existing == null) {
                    (recycled ?: GroupLayer(viewGroup).also { layers.add(it) })
                        .also { it.color = color }
                } else {
                    recycled?.let { disposeActiveLayer(it) }
                    existing
                }
            }

        next.add(proxy)
        proxy.layer = next

        activeLayers?.updateRecreateCount()
    }

    override fun removeProxy(proxy: ShadowProxy) {
        val layer =
            checkNotNull(proxy.layer as? GroupLayer) {
                "Overlay Proxy missing Layer"
            }

        proxy.layer = null
        layer.remove(proxy)
        if (!layer.isEmpty) return

        if (layer == inertLayer) {
            disposeInertLayer(layer)
        } else {
            disposeActiveLayer(layer)
            activeLayers?.updateRecreateCount()
        }

        if (inertLayer == null && activeLayers == null) dispose()
    }

    private fun disposeInertLayer(layer: GroupLayer) {
        inertLayer = null
        layer.dispose()
    }

    private fun disposeActiveLayer(layer: GroupLayer) {
        val layers = checkNotNull(activeLayers) { "LayerGroup is empty" }
        layers.remove(layer)
        layer.dispose()
        if (layers.isEmpty) activeLayers = null
    }
}