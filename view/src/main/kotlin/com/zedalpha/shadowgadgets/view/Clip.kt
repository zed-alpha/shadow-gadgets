package com.zedalpha.shadowgadgets.view

import android.graphics.Path
import android.view.View
import com.zedalpha.shadowgadgets.view.internal.MaterialShapeDrawableReflector
import com.zedalpha.shadowgadgets.view.internal.findMaterialShapeDrawable
import com.zedalpha.shadowgadgets.view.shadow.ShadowSwitch

/**
 * The current state of the clipped shadow fix for the receiver View.
 *
 * While this feature is active, the receiver's
 * [ViewOutlineProvider][android.view.ViewOutlineProvider] is wrapped
 * in a custom library implementation. Any user implementations should be set
 * before enabling this feature, or at least before the View attaches to its
 * Window.
 *
 * When true, the View's intrinsic shadow is always disabled, even if the
 * clipped replacement cannot be drawn, for whatever reason.
 */
var View.clipOutlineShadow: Boolean
    get() = getTag(R.id.clip_outline_shadow) == true
    set(value) {
        if (clipOutlineShadow == value) return
        setTag(R.id.clip_outline_shadow, value)
        ShadowSwitch.notifyPropertyChanged(this)
    }

/**
 * An interface through which to set the clip [Path] for irregularly shaped
 * Views on API levels 30 and above.
 *
 * Views that are not circles, plain rectangles, or single-radius rounded
 * rectangles have their shapes defined by a Path field that became inaccessible
 * starting with Android R. For those cases, this interface and its
 * corresponding extension property – [pathProvider] – provide a fallback
 * mechanism through which it can be set manually.
 */
fun interface ViewPathProvider {
    /**
     * Called whenever the target View's shape cannot be determined internally.
     *
     * The [view] is the target itself, and the [path] is an empty
     * instance that should be set appropriately. If the Path is left empty, the
     * clipped shadow will not be drawn.
     */
    fun getPath(view: View, path: Path)
}

/**
 * The [ViewPathProvider] used by [clipOutlineShadow] for irregular shapes on
 * API levels 30+.
 */
var View.pathProvider: ViewPathProvider?
    get() = getTag(R.id.path_provider) as? ViewPathProvider
    set(value) {
        if (pathProvider == value) return
        setTag(R.id.path_provider, value)
        ShadowSwitch.recreateShadow(this)
    }

/**
 * An implementation of [ViewPathProvider] that sets the Path from a
 * [MaterialShapeDrawable](https://developer.android.com/reference/com/google/android/material/shape/MaterialShapeDrawable)
 * in the target's background.
 *
 * If a MaterialShapeDrawable cannot be found, the Path is left unchanged.
 */
val MaterialComponentsViewPathProvider = ViewPathProvider { view, path ->
    val background = view.background ?: return@ViewPathProvider
    findMaterialShapeDrawable(background)?.let { msd ->
        MaterialShapeDrawableReflector.setPathFromDrawable(path, msd)
    }
}