package com.zedalpha.shadowgadgets.compose

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.layer.CompositingStrategy
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.currentValueOf

/**
 * This local controls a flag that can disable offscreen rendering for default
 * layers on API levels 24..28. This is relevant to the [clippedShadow] and
 * [clippedDropShadow] modifiers.
 *
 * Layers are handled internally, and they serve two distinct roles within the
 * library. Their primary application is color compat tinting, but they're also
 * used to work around a couple of bugs in the underlying system graphics on
 * 24..28, specifically:
 *
 * + `Matrix` operations – e.g., scale, rotate, etc. – are not applied correctly
 *   to clip `Path`s, potentially resulting in a different kind of artifact.
 *
 * + Certain combinations of clip-outs and layers can cause library shadows to
 *   be incorrectly clipped (normally) to their elements' rectangular bounds as
 *   well.
 *
 * The first bug requires offscreen rendering in order to fix it, the second
 * does not. Since there is no practical method for determining the current
 * `Matrix` at an arbitrary point in the draw routine, the library must assume
 * that all targets could be transformed, and it therefore defaults to
 * offscreen.
 *
 * If untinted shadows are known to be untransformed – not counting simple
 * translations – this flag can be set to `true` around them in order to enable
 * inline rendering on 24..28, avoiding the more expensive offscreen operation.
 * This flag is ignored when the shadow is tinted for color compat, as that
 * requires offscreen rendering.
 *
 * This functionality is most helpful around `LazyColumn`s and the like; setups
 * with few or static elements likely will not benefit much from it.
 */
public val LocalInlineDefaultShadowLayers: ProvidableCompositionLocal<Boolean> =
    compositionLocalOf { false }

internal fun CompositionLocalConsumerModifierNode.currentDefaultLayerCompositingStrategy(): CompositingStrategy =
    if (this.currentValueOf(LocalInlineDefaultShadowLayers)) {
        CompositingStrategy.ModulateAlpha
    } else {
        CompositingStrategy.Offscreen
    }