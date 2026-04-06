package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.ui.unit.Density

internal interface MutableDensity : Density {
    override var density: Float
    override var fontScale: Float
}

internal fun MutableDensity(density: Float, fontScale: Float): MutableDensity =
    MutableDensityImpl(density, fontScale)

private class MutableDensityImpl(
    override var density: Float,
    override var fontScale: Float
) : MutableDensity