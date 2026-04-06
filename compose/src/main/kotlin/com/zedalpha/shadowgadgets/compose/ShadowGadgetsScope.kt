package com.zedalpha.shadowgadgets.compose

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density

/**
 * The base scope interface for the lambda overloads of [clippedShadow] and
 * [shadowCompat]. All of its mutable properties can be modified without causing
 * recompositions.
 */
public interface ShadowGadgetsScope : Density {

    /**
     * Corresponds to [shadow][androidx.compose.ui.draw.shadow]'s `elevation`.
     */
    public var elevation: Float

    /**
     * Corresponds to [shadow][androidx.compose.ui.draw.shadow]'s
     * `ambientColor`.
     */
    public var ambientColor: Color

    /**
     * Corresponds to [shadow][androidx.compose.ui.draw.shadow]'s `spotColor`.
     */
    public var spotColor: Color

    /**
     * Corresponds to [clippedShadow]'s and [shadowCompat]'s `colorCompat`.
     */
    public var colorCompat: Color

    /**
     * Corresponds to [clippedShadow]'s and [shadowCompat]'s `forceColorCompat`.
     */
    public var forceColorCompat: Boolean
}