package com.zedalpha.shadowgadgets.view

import android.view.View
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.plane.updatePlane
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy

/**
 * Options for where a target's library shadow is inserted into the View
 * hierarchy's draw routine.
 */
public enum class ShadowPlane {

    /**
     * Foreground shadows are drawn in the target View's parent's overlay.
     *
     * This plane is the default.
     */
    Foreground,

    /**
     * Background shadows are drawn immediately after the target's parent's
     * background, behind all of its children.
     */
    Background,

    /**
     * Inline shadows are drawn right along with the target View itself.
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

    public companion object {

        internal fun forValue(value: Int) = ShadowPlane.entries[value]
    }
}

/**
 * The current [ShadowPlane] for the receiver [View]'s library shadow.
 */
public var View.shadowPlane: ShadowPlane
        by viewTag(R.id.shadow_plane, Foreground) {
            this.shadowProxy?.let { updatePlane(it) }
        }