package com.zedalpha.shadowgadgets.view

import android.view.View
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.shadow.recreateShadow


var View.shadowPlane: ShadowPlane
    get() = getTag(R.id.shadow_plane) as? ShadowPlane ?: Foreground
    set(value) {
        if (shadowPlane == value) return
        setTag(R.id.shadow_plane, value)
        recreateShadow()
    }

enum class ShadowPlane {

    Foreground, Background, Inline;

    internal companion object {
        fun forValue(value: Int) = values()[value]
    }
}

@Deprecated("Replaced by shadowPlane", ReplaceWith("shadowPlane"))
var View.clippedShadowPlane by View::shadowPlane

@Deprecated("Replaced by ShadowPlane", ReplaceWith("ShadowPlane"))
typealias ClippedShadowPlane = ShadowPlane