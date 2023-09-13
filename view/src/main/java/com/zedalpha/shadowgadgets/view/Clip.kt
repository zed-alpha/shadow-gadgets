package com.zedalpha.shadowgadgets.view

import android.graphics.Path
import android.view.View
import com.zedalpha.shadowgadgets.view.internal.MaterialShapeDrawableReflector
import com.zedalpha.shadowgadgets.view.internal.findMaterialShapeDrawable
import com.zedalpha.shadowgadgets.view.shadow.notifyPropertyChanged
import com.zedalpha.shadowgadgets.view.shadow.recreateShadow


var View.clipOutlineShadow: Boolean
    get() = getTag(R.id.clip_outline_shadow) == true
    set(value) {
        if (clipOutlineShadow == value) return
        setTag(R.id.clip_outline_shadow, value)
        notifyPropertyChanged()
    }

var View.pathProvider: ViewPathProvider?
    get() = getTag(R.id.path_provider) as? ViewPathProvider
    set(value) {
        if (pathProvider == value) return
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