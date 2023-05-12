package com.zedalpha.shadowgadgets.view

import android.graphics.Path
import android.view.View
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane.Foreground


var View.clipOutlineShadow: Boolean
    get() = getTag(R.id.clip_outline_shadow) == true
    set(value) {
        if (value == clipOutlineShadow) return
        setTag(R.id.clip_outline_shadow, value)
        ShadowSwitch.notifyClipChanged(this, value)
    }


var View.clippedShadowPlane: ClippedShadowPlane
    get() = getTag(R.id.shadow_plane) as? ClippedShadowPlane ?: Foreground
    set(value) {
        if (value == clippedShadowPlane) return
        setTag(R.id.shadow_plane, value)
        recreateShadow()
    }

enum class ClippedShadowPlane {

    Foreground, Background;

    val otherPlane: ClippedShadowPlane
        get() = if (this == Foreground) Background else Foreground

    internal companion object {
        fun forValue(value: Int) =
            if (value == 1) Background else Foreground
    }
}


var View.pathProvider: ViewPathProvider?
    get() = getTag(R.id.path_provider) as? ViewPathProvider
    set(value) {
        if (value == pathProvider) return
        setTag(R.id.path_provider, value)
        recreateShadow()
    }

fun interface ViewPathProvider {
    fun getPath(view: View, path: Path)
}

val MaterialComponentsViewPathProvider = ViewPathProvider { view, path ->
    val background = view.background ?: return@ViewPathProvider
    findMaterialShapeDrawable(background)?.let { msd ->
        MaterialShapeDrawableReflector.setPathFromDrawable(path, msd)
    }
}