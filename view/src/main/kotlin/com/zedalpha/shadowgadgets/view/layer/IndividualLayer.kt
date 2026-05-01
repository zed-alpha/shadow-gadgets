package com.zedalpha.shadowgadgets.view.layer

import android.view.View
import com.zedalpha.shadowgadgets.view.internal.OnMove
import com.zedalpha.shadowgadgets.view.internal.addOnMove
import com.zedalpha.shadowgadgets.view.internal.removeOnMove

internal abstract class IndividualLayer(
    private val owner: View,
    private val invalidate: () -> Unit,
    private val layer: Layer
) : Layer by layer {

    private val recreateLayer =
        OnMove {
            recreate()
            invalidate()
        }

    final override var color: Int
        get() = layer.color
        set(next) {
            val layer = this.layer
            if (layer.color == next) return

            val nextOffscreen = next.requiresOffscreenLayer()
            if (layer.isOffscreen != nextOffscreen) {
                if (nextOffscreen) owner.addOnMove(recreateLayer)
                else owner.removeOnMove(recreateLayer)
            }

            layer.color = next
        }
}