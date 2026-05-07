package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.ui.unit.Density

internal fun MutableDensity(density: Float, fontScale: Float): MutableDensity =
    MutableDensityImpl(density, fontScale)

internal interface MutableDensity : Density {
    override var density: Float
    override var fontScale: Float
}

private class MutableDensityImpl(
    override var density: Float,
    override var fontScale: Float
) : MutableDensity