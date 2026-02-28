package com.zedalpha.shadowgadgets.view

import android.view.View
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy
import com.zedalpha.shadowgadgets.view.proxy.updatePlane

/**
 * Options for locations in the [View] hierarchy's draw routine where a target's
 * library shadow can be inserted.
 */
public enum class ShadowPlane {

    /**
     * Shadows in this Plane are drawn in the target View's parent's overlay.
     *
     * This plane is the default.
     */
    Foreground,

    /**
     * Shadows here are drawn immediately after the target's parent's
     * background drawable, behind all the children.
     */
    Background,

    /**
     * This plane's shadows are drawn right along with the target View itself.
     *
     * Use of this plane requires that either:
     *
     * + The target View has [clipToOutline][android.view.View.setClipToOutline]
     *   set to `false`, and the parent ViewGroup has
     *   [clipChildren][android.view.ViewGroup.setClipChildren] set to `false`.
     *
     * + Or, the parent is one of the library's specialized
     *   [ShadowsViewGroup][com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup]s
     *   with
     *   [takeOverDrawForInlineChildShadows][com.zedalpha.shadowgadgets.view.viewgroup.ShadowsViewGroup.takeOverDrawForInlineChildShadows]
     *   set to `true`.
     *
     * Refer to
     * [ShadowPlane's wiki page](https://github.com/zed-alpha/shadow-gadgets/wiki/ShadowPlane#inline-plane)
     * for further details.
     */
    Inline;

    public companion object
}

/**
 * The current [ShadowPlane] for the receiver [View]'s library shadow.
 */
public var View.shadowPlane: ShadowPlane
        by viewTag(R.id.shadow_plane, Foreground) {
            this.shadowProxy?.updatePlane()
        }