package com.zedalpha.shadowgadgets.compose.internal

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import com.zedalpha.shadowgadgets.compose.ShadowGadgetsScope

internal interface WorkingShadowScope : ShadowGadgetsScope, MutableDensity

internal fun WorkingShadowScope.isColorCompatDefault(): Boolean =
    this.colorCompat.isOrBlendsToDefault(this.ambientColor, this.spotColor)
        .also { Log.d("QQQ", "$it, $colorCompat, $ambientColor, $spotColor") }

internal abstract class WorkingShadowScopeImpl(private val defaultColorCompat: Color) :
    WorkingShadowScope {

    final override var density: Float = 1F
    final override var fontScale: Float = 1F

    final override var elevation: Float = 0F
    final override var ambientColor: Color = DefaultShadowColor
    final override var spotColor: Color = DefaultShadowColor
    final override var colorCompat: Color = defaultColorCompat
    final override var forceColorCompat: Boolean = false

    fun reset() {
        elevation = 0F
        ambientColor = DefaultShadowColor
        spotColor = DefaultShadowColor
        colorCompat = defaultColorCompat
        forceColorCompat = false
    }
}