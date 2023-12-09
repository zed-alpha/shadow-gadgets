package com.zedalpha.shadowgadgets.view

import android.view.View
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.shadow.recreateShadow


/**
 * Determines where a target View's library shadow is inserted into the
 * hierarchy's draw routine.
 */
enum class ShadowPlane {
    /**
     * Foreground shadows are drawn in the target View's parent's overlay.
     *
     * Shadows in this plane are clipped by the hierarchy's draw routine in the
     * same way that the target View itself is.
     *
     * This plane is the default.
     */
    Foreground,

    /**
     * Background shadows are drawn immediately after the target's parent's
     * background, behind all of its children.
     *
     * Shadows in this plane are always clipped to the parent's bounds, no
     * matter how the target is otherwise clipped by the hierarchy's draw routine.
     */
    Background,

    /**
     * Inline shadows are drawn right along with the target View itself.
     *
     * Shadows in this plane are clipped by the hierarchy's draw routine in the
     * same way that the target View itself is.
     *
     * Use of this plane requires that either:
     *
     * + The target View has [clipToOutline][android.view.View.setClipToOutline]
     *   set to false, and the parent ViewGroup has
     *   [clipChildren][android.view.ViewGroup.setClipChildren] set to false.
     *
     * + Or, the parent is one of the library's specialized
     *   [ShadowsViewGroup][com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup]s
     *   with
     *   [ignoreInlineChildShadows][com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup.ignoreInlineChildShadows]
     *   set to false.
     *
     * Refer to
     * [ShadowPlane's wiki page](https://github.com/zed-alpha/shadow-gadgets/wiki/ShadowPlane#inline-plane)
     * for further details.
     */
    Inline;

    internal companion object {
        // Maps XML attribute values to enum values.
        fun forValue(value: Int) = entries[value]
    }
}

/**
 * The current [ShadowPlane] for the receiver View's library shadow.
 */
var View.shadowPlane: ShadowPlane
    get() = getTag(R.id.shadow_plane) as? ShadowPlane ?: Foreground
    set(value) {
        if (shadowPlane == value) return
        setTag(R.id.shadow_plane, value)
        recreateShadow()
    }

/**
 * Replaced by [ShadowPlane]
 */
@Deprecated("Replaced by ShadowPlane", ReplaceWith("ShadowPlane"))
typealias ClippedShadowPlane = ShadowPlane

/**
 * Replaced by [shadowPlane]
 */
@Suppress("DEPRECATION")
@Deprecated("Replaced by shadowPlane", ReplaceWith("shadowPlane"))
var View.clippedShadowPlane: ClippedShadowPlane by View::shadowPlane