package com.zedalpha.shadowgadgets.view

import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory

/**
 * Helper object that allows for sensible namespacing of standalone or otherwise
 * unique miscellanea.
 */
public object ShadowGadgets {

    /**
     * Indicates whether the primary draw method is available in the current
     * environment.
     */
    public val isPrimaryDrawMethodAvailable: Boolean
        get() = RenderNodeFactory.isCapable

    /**
     * Indicates whether the primary draw method is currently enabled for use.
     *
     * The
     * [forceFallbackDrawMethod][com.zedalpha.shadowgadgets.view.ShadowGadgets.forceFallbackDrawMethod]
     * flag affects this value; i.e., if that flag is `true`, this will return
     * `false`.
     *
     * THe draw method for a given shadow instance is determined during its
     * initialization, so this value does not necessarily mean that all current
     * library shadows are using the indicated method, e.g, if
     * [forceFallbackDrawMethod][com.zedalpha.shadowgadgets.view.ShadowGadgets.forceFallbackDrawMethod]
     * has been toggled.
     */
    public val isPrimaryDrawMethodEnabled: Boolean
        get() = RenderNodeFactory.isOpen

    /**
     * A flag to force the fallback draw method, which may be useful in testing
     * and debugging. The fallback is normally used only on API levels 21..28
     * whenever the library is unable to access the (hidden) classes and methods
     * necessary for the primary method.
     *
     * This flag should be set as early as possible, e.g., in an
     * [Application][android.app.Application] subclass.
     *
     * This is a passive flag; changing it does _not_ recreate existing library
     * shadows. If it needs to be toggled at runtime, those shadows will need to
     * be toggled as well. In order to ensure that the library's internals sync
     * correctly, all active shadows in a given hierarchy must be disabled all
     * at once first. Recreating the [Activity][android.app.Activity] or
     * [Dialog][android.app.Dialog] might be the easiest way to handle this.
     */
    public var forceFallbackDrawMethod: Boolean
        get() = !RenderNodeFactory.isEnabled
        set(forceFallback) {
            RenderNodeFactory.isEnabled = !forceFallback
        }

    /**
     * A flag for the library's Exception behavior if the target has no
     * [doOnShadowModeChange] set. If one is set, it's assumed that
     * [ShadowMode.Error]s will be handled there.
     *
     * When `true`, invalid states will cause [ShadowException]s to be thrown;
     * otherwise execution falls through to the logs as described for `false`.
     * When `false`, no Exceptions are thrown, and logs are printed instead in
     * debug builds, as long as
     * [suppressLogs][com.zedalpha.shadowgadgets.view.ShadowGadgets.suppressLogs]
     * is `false`.
     *
     * While `true`, the [View.updateShadow][android.view.View.updateShadow]
     * function should be used whenever modifying multiple properties at once.
     */
    public var throwOnUnhandledErrors: Boolean = false

    /**
     * By default, messages for known error states are logged in debug builds.
     * This flag allows those logs to be suppressed.
     *
     * If the library throws due to
     * [throwOnUnhandledErrors][com.zedalpha.shadowgadgets.view.ShadowGadgets.throwOnUnhandledErrors],
     * those logs are skipped anyway, since the [ShadowException] messages would
     * be identical.
     *
     * No logs are printed in release builds.
     */
    public var suppressLogs: Boolean = false
}

/**
 * General [Exception] class for known error states.
 */
public class ShadowException(message: String) : RuntimeException(message)