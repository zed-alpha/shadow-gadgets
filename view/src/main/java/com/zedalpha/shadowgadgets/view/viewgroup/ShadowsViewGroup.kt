@file:Suppress("DEPRECATION")

package com.zedalpha.shadowgadgets.view.viewgroup

import androidx.annotation.ColorInt
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.ShadowPlane


sealed interface ShadowsViewGroup : ClippedShadowsViewGroup {

    var childShadowsPlane: ShadowPlane

    @Deprecated(
        "Replaced by childShadowsPlane",
        ReplaceWith("childShadowsPlane")
    )
    override var childClippedShadowsPlane: ClippedShadowPlane

    @get:ColorInt
    @setparam:ColorInt
    var childOutlineShadowsColorCompat: Int

    var forceChildOutlineShadowsColorCompat: Boolean
}

@Deprecated("Replaced by ShadowsViewGroup")
sealed interface ClippedShadowsViewGroup {

    var clipAllChildShadows: Boolean

    var childClippedShadowsPlane: ClippedShadowPlane

    var ignoreInlineChildShadows: Boolean
}