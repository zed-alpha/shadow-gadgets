package com.zedalpha.shadowgadgets.view.shadow

import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.fastForEach
import com.zedalpha.shadowgadgets.core.isDefault
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

internal abstract class GroupController(protected val parentView: ViewGroup) :
    ShadowController(parentView) {

    protected val shadows = mutableListOf<GroupShadow>()

    protected val isRecyclingViewGroup = parentView.isRecyclingViewGroup

    override fun invalidate() = parentView.invalidate()

    final override fun onParentViewDetached() {
        if (isRecyclingViewGroup) detachAllShadows()
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

    // Must copy because detachFromTarget() modifies shadows.
    fun detachAllShadows() =
        shadows.toList().fastForEach { it.detachFromTarget() }

    private var colorLayers: MutableList<Layer>? = null

    override fun onCreateLayer(layer: Layer) {
        if (layer.color.isDefault) return

        val layers = colorLayers
            ?: mutableListOf<Layer>().also { colorLayers = it }
        layers += layer
    }

    override fun onDisposeLayer(layer: Layer) {
        colorLayers?.remove(layer)
        if (colorLayers?.isEmpty() == true) colorLayers = null
    }

    override fun hasColorLayer(): Boolean = colorLayers?.isNotEmpty() == true

    override fun recreateColorLayers() {
        colorLayers?.fastForEach { it.recreate() }
    }

    @CallSuper
    override fun onSizeChanged(width: Int, height: Int) {
        colorLayers?.fastForEach { it.setSize(width, height) }
    }

    @CallSuper
    override fun checkInvalidate() {
        if (!RenderNodeFactory.isOpen) colorLayers?.fastForEach { it.refresh() }
    }
}