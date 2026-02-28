package com.zedalpha.shadowgadgets.view

import android.view.View
import com.zedalpha.shadowgadgets.view.ShadowGadgets.suppressLogs
import com.zedalpha.shadowgadgets.view.ShadowGadgets.throwOnUnhandledErrors
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.plane.Plane
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy
import com.zedalpha.shadowgadgets.view.proxy.updatePlane

/**
 * General, all-purpose annotation for any public API that may change.
 */
@RequiresOptIn("Nonfinal API. May be altered, moved or removed without notice.")
@Retention(AnnotationRetention.BINARY)
public annotation class ExperimentalShadowGadgets

//region ShadowGadgets

/**
 * Helper object that allows for sensible namespacing of standalone or otherwise
 * unique miscellanea.
 */
@ExperimentalShadowGadgets
public object ShadowGadgets {

    /**
     * By default, messages for known error states are logged in debug builds
     * whenever the target has no [doOnShadowModeChange] set. This flag allows
     * those logs to be suppressed.
     *
     * If the library throws due to [throwOnUnhandledErrors], these logs are
     * skipped.
     *
     * No logs are printed in release builds.
     */
    @ExperimentalShadowGadgets
    public var suppressLogs: Boolean = false

    /**
     * A flag for the library's Exception behavior if the target has no
     * [doOnShadowModeChange] set. If one is set, it's assumed that
     * [ShadowMode.Error]s will be handled there.
     *
     * When `true`, invalid states will cause [ShadowException]s to be thrown;
     * otherwise execution falls through to the logs as described for `false`.
     *
     * When `false`, no Exceptions are thrown, and instead logs are printed in
     * debug builds, as long [suppressLogs] is `false`.
     *
     * Whenever this is `true`, the [View.updateShadow] function should be used
     * whenever modifying multiple properties at once.
     */
    @ExperimentalShadowGadgets
    public var throwOnUnhandledErrors: Boolean = false
}

//endregion

//region ShadowException

/**
 * General [Exception] class for known possible error states.
 */
@ExperimentalShadowGadgets
public class ShadowException(message: String) : RuntimeException(message)

//endregion

//region ShadowMode

/**
 * The possible shadow modes.
 */
@ExperimentalShadowGadgets
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
@ExperimentalShadowGadgets
public val View.shadowMode: ShadowMode
    get() = this.shadowProxy?.plane?.shadowMode ?: ShadowMode.Native

@ExperimentalShadowGadgets
internal inline val Plane.shadowMode: ShadowMode
    get() = if (this == Plane.Null) ShadowMode.Error else ShadowMode.Library

/**
 * Callback for [ShadowMode] changes. The target [View] is passed as the
 * receiver for [action], allowing for unqualified access to its members.
 *
 * It is recommended to handle this in the same manner as `View` updates in
 * recycling `Adapter`s: always account for all possible states, since
 * multi-property updates could potentially involve invalid intermediate states.
 * For example, do _not_ do this:
 *
 * ```kotlin
 * target.doOnShadowModeChange { mode ->
 *     if (mode == ShadowMode.Disabled) {
 *         foreground = FallbackShadowDrawable()
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
 *             FallbackShadowDrawable()
 *         } else {
 *             null
 *         }
 * }
 * ```
 *
 * **NB:** This is a setter. A maximum of one [action] per [View] is supported.
 */
@ExperimentalShadowGadgets
public fun View.doOnShadowModeChange(action: (View.(mode: ShadowMode) -> Unit)?) {
    this.onShadowModeChange = action
}

@ExperimentalShadowGadgets
internal var View.onShadowModeChange: (View.(ShadowMode) -> Unit)?
        by viewTag(R.id.on_shadow_mode_change, null)

//endregion

//region Shadow update

/**
 * Allows for efficient modification of multiple shadow properties at once,
 * pausing potentially redundant internal operations until [block] completes.
 * This functionality is particularly helpful in recycling Adapters that set
 * more than one shadow property.
 *
 * The target [View] itself is passed as [block]'s receiver, allowing for
 * unqualified access to its properties. The receiver is not restricted in any
 * way, and all of its members may be accessed like normal within [block],
 * but the library pauses only its own operations.
 *
 * This function should always be used to modify multiple properties whenever
 * [ShadowGadgets.throwOnUnhandledErrors] is `true`, in order to avoid potential
 * illegal states during multistep updates.
 */
@ExperimentalShadowGadgets
public fun View.updateShadow(block: View.() -> Unit) {
    this.isInShadowUpdate = true
    this.block()
    this.isInShadowUpdate = false
    this.shadowProxy?.updatePlane()
}

internal var View.isInShadowUpdate: Boolean
        by viewTag(R.id.is_in_shadow_update, false)

/**
 * Convenience for completely resetting a target's shadow state, removing the
 * library shadow if present, and restoring the (presumably broken) native one.
 *
 * This is unlikely to be useful in production, but it might be handy for
 * testing and debugging.
 */
@ExperimentalShadowGadgets
public fun View.resetShadow(): Unit =
    this.updateShadow {
        shadowPlane = ShadowPlane.Foreground
        clipOutlineShadow = false
        outlineShadowColorCompat = DefaultShadowColor
        forceOutlineShadowColorCompat = false
    }

//endregion