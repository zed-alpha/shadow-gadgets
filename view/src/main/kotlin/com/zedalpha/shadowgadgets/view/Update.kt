package com.zedalpha.shadowgadgets.view

import android.view.View
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy
import com.zedalpha.shadowgadgets.view.proxy.updatePlane

/**
 * Allows for efficient modification of multiple shadow properties at once,
 * pausing potentially redundant internal operations until [block] completes.
 * This functionality is particularly helpful in recycling Adapters that set
 * more than one shadow property.
 *
 * The target [View] itself is passed as [block]'s receiver, allowing for
 * unqualified access to its properties. The receiver is not restricted in any
 * way, and all its members may be accessed like normal within [block], but the
 * library pauses only its own operations.
 *
 * This function should always be used to modify multiple properties whenever
 * [ShadowGadgets.throwOnUnhandledErrors] is `true`, in order to avoid potential
 * illegal states during multistep updates.
 */
public fun View.updateShadow(block: View.() -> Unit) {
    this.isInShadowUpdate = true
    this.block()
    this.isInShadowUpdate = false
    this.shadowProxy?.updatePlane()
}

internal var View.isInShadowUpdate: Boolean
        by viewTag(R.id.is_in_shadow_update, false)

/**
 * A convenience function that utilizes [updateShadow] to revert a target's
 * library shadow properties to their default values, restoring the (presumably
 * broken) native shadow.
 */
public fun View.resetShadow(): Unit =
    this.updateShadow {
        shadowPlane = ShadowPlane.Foreground
        clipOutlineShadow = false
        outlineShadowColorCompat = DefaultShadowColor
        forceOutlineShadowColorCompat = false
    }