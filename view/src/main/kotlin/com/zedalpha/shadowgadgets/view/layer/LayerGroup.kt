package com.zedalpha.shadowgadgets.view.layer

import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.internal.Group
import com.zedalpha.shadowgadgets.view.internal.ListGroup
import com.zedalpha.shadowgadgets.view.internal.OnMove
import com.zedalpha.shadowgadgets.view.internal.addOnMove
import com.zedalpha.shadowgadgets.view.internal.isTint
import com.zedalpha.shadowgadgets.view.internal.removeOnMove
import com.zedalpha.shadowgadgets.view.plane.Plane

internal class LayerGroup<T : Layer>(
    private val viewGroup: ViewGroup,
    private val plane: Plane
) : Group<T> by ListGroup() {

    private val recreateLayers =
        OnMove {
            iterate { it.recreate() }
            plane.invalidate()
        }

    private var recreateCount = 0
        set(value) {
            if (field == value) return
            check(value >= 0) { "recreateCount cannot be negative: $value" }

            if (field == 0) viewGroup.addOnMove(recreateLayers)
            if (value == 0) viewGroup.removeOnMove(recreateLayers)

            field = value
        }

    fun updateRecreateCount() {
        var count = 0
        iterate { if (it.color.isTint) count++ }
        recreateCount = count
    }
}