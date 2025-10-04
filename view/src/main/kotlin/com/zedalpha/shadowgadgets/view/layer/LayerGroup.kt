package com.zedalpha.shadowgadgets.view.layer

import android.view.ViewGroup
import com.zedalpha.shadowgadgets.view.internal.OnMove
import com.zedalpha.shadowgadgets.view.internal.SwitchGroup
import com.zedalpha.shadowgadgets.view.internal.addOnMove
import com.zedalpha.shadowgadgets.view.internal.isTint
import com.zedalpha.shadowgadgets.view.internal.removeOnMove
import com.zedalpha.shadowgadgets.view.plane.Plane

internal class LayerGroup<T : Layer>(
    private val viewGroup: ViewGroup,
    private val plane: Plane
) : SwitchGroup<T>() {

    private val recreateLayers =
        OnMove {
            var invalidate = false
            iterate { invalidate = it.recreate() || invalidate }
            if (invalidate) plane.invalidate()
        }

    private var recreateCount = 0
        set(value) {
            if (field == value) return
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