package com.zedalpha.shadowgadgets.view.layer

import android.view.View
import com.zedalpha.shadowgadgets.view.internal.OnMove
import com.zedalpha.shadowgadgets.view.internal.addOnMove
import com.zedalpha.shadowgadgets.view.internal.isTint
import com.zedalpha.shadowgadgets.view.internal.removeOnMove

internal abstract class IndividualLayer(
    private val owner: View,
    private val invalidate: () -> Unit,
    private val layer: Layer
) : Layer by layer {

    private val recreateLayer = OnMove { recreate(); invalidate() }

    final override var color: Int
        get() = layer.color
        set(value) {
            val field = layer.color
            if (field == value) return
            if (field.isTint != value.isTint) {
                if (value.isTint) owner.addOnMove(recreateLayer)
                if (field.isTint) owner.removeOnMove(recreateLayer)
            }
            layer.color = value
        }
}