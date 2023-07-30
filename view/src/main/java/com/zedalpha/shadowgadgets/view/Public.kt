package com.zedalpha.shadowgadgets.view

import android.graphics.Path
import android.view.View
import com.zedalpha.shadowgadgets.view.internal.MaterialShapeDrawableReflector
import com.zedalpha.shadowgadgets.view.internal.findMaterialShapeDrawable
import com.zedalpha.shadowgadgets.view.shadow.ShadowSwitch
import com.zedalpha.shadowgadgets.view.shadow.recreateShadow


var View.clipOutlineShadow: Boolean
    get() = getTag(R.id.clip_outline_shadow) == true
    set(value) {
        if (value == clipOutlineShadow) return
        setTag(R.id.clip_outline_shadow, value)
        ShadowSwitch.notifyPropertyChanged(this)
    }


var View.clippedShadowPlane: ClippedShadowPlane
    get() = getTag(R.id.clipped_shadow_plane) as? ClippedShadowPlane
        ?: ClippedShadowPlane.Foreground
    set(value) {
        if (value == clippedShadowPlane) return
        setTag(R.id.clipped_shadow_plane, value)
        recreateShadow()
    }

enum class ClippedShadowPlane {

    Foreground, Background, Inline;

    internal companion object {
        fun forValue(value: Int) = when (value) {
            1 -> Background
            2 -> Inline
            else -> Foreground
        }
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