package com.zedalpha.shadowgadgets.view

import android.view.View
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.plane.Plane
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy

/**
 * The possible shadow modes.
 */
public enum class ShadowMode {

    /**
     * The native shadow is in place.
     */
    Native,

    /**
     * A library shadow is attached and working normally.
     */
    Library,

    /**
     * The library encountered an issue and has disabled the shadow.
     */
    Error
}

/**
 * The current [ShadowMode] of the receiver's shadow.
 */
public val View.shadowMode: ShadowMode
    get() = this.shadowProxy?.plane?.shadowMode ?: ShadowMode.Native

internal inline val Plane.shadowMode: ShadowMode
    get() = if (this === Plane.Null) ShadowMode.Error else ShadowMode.Library

/**
 * Callback for [ShadowMode] changes. The target [View] is passed as the
 * receiver for [action], allowing for unqualified access to its members.
 *
 * This is a persistent callback, not a one-shot event. To disable, call again
 * passing `null` for the [action].
 *
 * It is recommended to handle this in the same manner as `View` updates in
 * recycling `Adapter`s: always account for all possible modes, since
 * multi-property updates could potentially involve invalid intermediate states.
 *
 * For example, do _not_ do this:
 *
 * ```kotlin
 * target.doOnShadowModeChange { mode ->
 *     if (mode == ShadowMode.Disabled) {
 *         foreground = FallbackDrawable()
 *     }
 * }
 * ```
 *
 * Instead, do this:
 *
 * ```kotlin
 * target.doOnShadowModeChange { mode ->
 *     foreground =
 *         if (mode == ShadowMode.Disabled) {
 *             FallbackDrawable()
 *         } else {
 *             null
 *         }
 * }
 * ```
 */
public fun View.doOnShadowModeChange(action: (View.(mode: ShadowMode) -> Unit)?) {
    this.onShadowModeChange = action
}

internal var View.onShadowModeChange: (View.(ShadowMode) -> Unit)?
        by viewTag(R.id.on_shadow_mode_change, null)