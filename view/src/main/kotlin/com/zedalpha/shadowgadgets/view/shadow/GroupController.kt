package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.fastForEach
import com.zedalpha.shadowgadgets.core.isNotDefault
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

internal abstract class GroupController(protected val parentView: ViewGroup) :
    ShadowController(parentView, parentView) {

    protected val shadows = mutableListOf<GroupShadow>()

    protected val isRecyclingViewGroup = parentView.isRecyclingViewGroup

    final override fun onParentViewDetached() {
        if (isRecyclingViewGroup) {
            // Must copy because detachFromTarget() modifies shadows.
            shadows.toList().fastForEach { it.detachFromTarget() }
        }
    }

    fun createShadow(target: View) {
        val plane = providePlane(target)
        shadows += GroupShadow(target, this, plane)
    }

    protected abstract fun providePlane(target: View): DrawPlane

    fun disposeShadow(shadow: GroupShadow) {
        if (shadows.remove(shadow) && shadows.isEmpty()) onEmpty()
    }

    protected open fun onEmpty() {}

    private var coreLayers: MutableList<Layer>? = null

    private var colorLayerCount = 0

    override fun onCreateLayer(layer: Layer) {
        layer.setSize(parentView.width, parentView.height)

        val layers = coreLayers
            ?: mutableListOf<Layer>().also { coreLayers = it }
        layers += layer

        if (layer.color.isNotDefault) colorLayerCount++
    }

    override fun onDisposeLayer(layer: Layer) {
        coreLayers?.remove(layer)
        if (coreLayers?.isEmpty() == true) coreLayers = null

        if (layer.color.isNotDefault) colorLayerCount--
    }

    override fun hasColorLayer(): Boolean = colorLayerCount > 0

    override fun recreateColorLayers() {
        coreLayers?.fastForEach { it.recreate() }
    }

    @CallSuper
    override fun onLayerSizeChanged(width: Int, height: Int) {
        coreLayers?.fastForEach { it.setSize(width, height) }
    }

    @CallSuper
    override fun checkInvalidate() {
        if (!RenderNodeFactory.isOpen) coreLayers?.fastForEach { it.refresh() }
    }

    override fun invalidate() = parentView.invalidate()
}