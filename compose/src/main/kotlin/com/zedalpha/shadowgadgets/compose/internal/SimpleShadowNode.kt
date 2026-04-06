package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

internal abstract class SimpleShadowNode(
    var elevationDp: Dp,
    shape: Shape,
    final override var ambientColor: Color,
    final override var spotColor: Color,
    final override var colorCompat: Color,
    final override var forceColorCompat: Boolean
) : ShadowNode(shape), WorkingShadowScope {

    final override var density: Float = 1F
    final override var fontScale: Float = 1F

    final override var elevation: Float
        get() = elevationDp.toPx()
        set(value) {
            elevationDp = value.toDp()
        }

    final override fun onAttach() {
        super.onAttach()
        update(shape)
    }

    final override val shadowScope: WorkingShadowScope get() = this

    fun update(shape: Shape) {
        val invalidateDrawCache = this.shape != shape
        this.shape = shape

        val invalidateDraw = updateShadow()

        when {
            invalidateDrawCache -> invalidateDrawCache()
            invalidateDraw -> invalidateDraw()
        }
    }
}