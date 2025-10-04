package com.zedalpha.shadowgadgets.view

import android.view.View
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.plane.NullPlane
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy

/**
 * Generic, all-purpose annotation for any public API that may change.
 */
@RequiresOptIn("Experimental API. May be altered or removed without notice.")
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalShadowGadgets


/**
 * A simple callback that fires whenever a library shadow changes its specific
 * attachment, which isn't known until its target [View] attaches to its
 * [Window][android.view.Window], and which can change whenever relevant
 * properties are modified.
 *
 * [action]'s parameter `isDrawn` will be true normally, and false if the
 * library determines that it cannot draw, for whatever reason. In the latter
 * case, the target's intrinsic shadow will still be disabled. This feature
 * provides the opportunity to employ a fallback drawable, etc.
 *
 * **NB:** This is a setter. Only one attach action is allowed at a time.
 */
@ExperimentalShadowGadgets
public fun View.onShadowAttach(action: ((isDrawn: Boolean) -> Unit)?) {
    this.onShadowAttach = action
}

internal var View.onShadowAttach: ((Boolean) -> Unit)?
        by viewTag(R.id.on_shadow_attach, null)


/**
 * Descriptions of a target's shadow's draw state.
 */
@ExperimentalShadowGadgets
public enum class ShadowDrawMode {

    /**
     * The native shadow is in place; i.e., no library shadow is active.
     */
    Native,

    /**
     * A library shadow is attached and drawing normally.
     */
    Normal,

    /**
     * The library discovered an issue and has disabled the draw.
     */
    Disabled
}

/**
 * The current [ShadowDrawMode] of the receiver's [View]'s shadow.
 */
@ExperimentalShadowGadgets
public val View.shadowDrawMode: ShadowDrawMode
    get() {
        val proxy = this.shadowProxy
        return when {
            proxy == null -> ShadowDrawMode.Native
            proxy.plane == NullPlane -> ShadowDrawMode.Disabled
            else -> ShadowDrawMode.Normal
        }
    }