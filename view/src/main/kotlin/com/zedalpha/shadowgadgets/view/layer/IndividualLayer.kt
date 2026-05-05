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

    private var recreateOnMove: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) owner.addOnMove(recreateLayer)
            else owner.removeOnMove(recreateLayer)
        }

    final override var color: Int
        get() = layer.color
        set(next) {
            val layer = this.layer
            if (layer.color == next) return
            layer.color = next
            recreateOnMove = isOffscreen
        }
}