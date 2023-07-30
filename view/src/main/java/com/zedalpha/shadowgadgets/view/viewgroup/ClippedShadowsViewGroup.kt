package com.zedalpha.shadowgadgets.view.viewgroup

import com.zedalpha.shadowgadgets.view.ClippedShadowPlane

sealed interface ClippedShadowsViewGroup {
    var clipAllChildShadows: Boolean
    var childClippedShadowsPlane: ClippedShadowPlane
    var ignoreInlineChildShadows: Boolean
}