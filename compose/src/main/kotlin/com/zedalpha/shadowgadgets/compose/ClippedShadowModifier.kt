package com.zedalpha.shadowgadgets.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.internal.BlockShadowNode
import com.zedalpha.shadowgadgets.compose.internal.SimpleShadowNode
import com.zedalpha.shadowgadgets.compose.internal.WorkingShadowScopeImpl

/**
 * Creates a replacement for the regular
 * [shadow][androidx.compose.ui.draw.shadow] modifier with the interior artifact
 * clipped out.
 *
 * Refer to [shadow][androidx.compose.ui.draw.shadow]'s docs for parameter
 * details.
 *
 * **NB:** This should _not_ be used for shadows that extend beyond the root
 * composable's bounds on API levels 24..28. The clip feature requires a layer
 * on those versions due to a bug in the system graphics, and layers can be no
 * larger than the root.
 */
@Stable
public fun Modifier.clippedShadow(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor
): Modifier =
    clippedShadow(
        elevation = elevation,
        shape = shape,
        clip = clip,
        ambientColor = ambientColor,
        spotColor = spotColor,
        colorCompat = DefaultShadowColor,
        forceColorCompat = false
    )

/**
 * A [clippedShadow] overload that can be tinted with the library's color compat
 * mechanism on API levels before 28, the earliest version to support the native
 * shadow colors.
 *
 * Refer to [shadow][androidx.compose.ui.draw.shadow]'s docs for details on the
 * first five parameters: [elevation], [shape], [clip], [ambientColor], and
 * [spotColor].
 *
 * [colorCompat] takes a [Color] that's used to tint the shadow on API levels
 * 27 and below. If the passed value is [Color.Black] – the default shadow color
 * – no tint is applied. If [Color.Unspecified] is passed, the actual tint is
 * calculated as a blend of the [ambientColor] and [spotColor], mixed in
 * proportion to their current theme alphas. Setting any other value disables
 * this blending behavior.
 *
 * [forceColorCompat] is available to enable the effect on API levels 28 and
 * above.
 *
 * The color blending formula gives good results only if the ambient and spot
 * colors are both fully opaque; i.e., only if both have maximum alpha values.
 *
 * **NB:** This should _not_ be used for shadows that extend beyond the root
 * composable's bounds on API levels 24..28. The clip feature requires a
 * compositing layer on those versions due to a bug in the system graphics, and
 * layers can be no larger than the root.
 *
 * Color compat, on any API level, has the same restriction, as its tint is
 * applied through a compositing layer.
 */
@Stable
public fun Modifier.clippedShadow(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor,
    colorCompat: Color = DefaultShadowColor,
    forceColorCompat: Boolean = false
): Modifier {
    val modifier =
        if (elevation > 0.dp) {
            this then ClippedShadowElement(
                elevation = elevation,
                shape = shape,
                clip = clip,
                ambientColor = ambientColor,
                spotColor = spotColor,
                colorCompat = colorCompat,
                forceColorCompat = forceColorCompat
            )
        } else {
            this
        }

    return if (clip) modifier.clip(shape) else modifier
}

private class ClippedShadowElement(
    private val elevation: Dp,
    private val shape: Shape,
    private val clip: Boolean,
    private val ambientColor: Color,
    private val spotColor: Color,
    private val colorCompat: Color,
    private val forceColorCompat: Boolean
) : ModifierNodeElement<SimpleClippedShadowNode>() {

    override fun create(): SimpleClippedShadowNode =
        SimpleClippedShadowNode(
            elevation = elevation,
            shape = shape,
            ambientColor = ambientColor,
            spotColor = spotColor,
            colorCompat = colorCompat,
            forceColorCompat = forceColorCompat
        )

    override fun update(node: SimpleClippedShadowNode) {
        node.elevationDp = elevation
        node.ambientColor = ambientColor
        node.spotColor = spotColor
        node.colorCompat = colorCompat
        node.forceColorCompat = forceColorCompat
        node.update(shape)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "clippedShadow"
        properties["elevation"] = elevation
        properties["shape"] = shape
        properties["clip"] = clip
        properties["ambientColor"] = ambientColor
        properties["spotColor"] = spotColor
        properties["colorCompat"] = colorCompat
        properties["forceColorCompat"] = forceColorCompat
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ClippedShadowElement) return false
        if (elevation != other.elevation) return false
        if (shape != other.shape) return false
        if (clip != other.clip) return false
        if (ambientColor != other.ambientColor) return false
        if (spotColor != other.spotColor) return false
        if (colorCompat != other.colorCompat) return false
        if (forceColorCompat != other.forceColorCompat) return false
        return true
    }

    override fun hashCode(): Int {
        var result = elevation.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + clip.hashCode()
        result = 31 * result + ambientColor.hashCode()
        result = 31 * result + spotColor.hashCode()
        result = 31 * result + colorCompat.hashCode()
        result = 31 * result + forceColorCompat.hashCode()
        return result
    }
}

private class SimpleClippedShadowNode(
    elevation: Dp,
    shape: Shape,
    ambientColor: Color,
    spotColor: Color,
    colorCompat: Color,
    forceColorCompat: Boolean
) : SimpleShadowNode(
    elevationDp = elevation,
    shape = shape,
    ambientColor = ambientColor,
    spotColor = spotColor,
    colorCompat = colorCompat,
    forceColorCompat = forceColorCompat
) {
    override val isClipped: Boolean get() = true

    private val clip = Path()

    override fun onSetOutline(outline: Outline) =
        clip.run { rewind(); addOutline(outline) }

    override fun drawShadow(scope: DrawScope) =
        scope.clipPath(clip, ClipOp.Difference) { super.drawShadow(this) }
}

/**
 * The specific scope for the lambda overload of [clippedShadow].
 */
public interface ClippedShadowScope : ShadowGadgetsScope

/**
 * The lambda version of [clippedShadow] that allows for modification of shadow
 * properties without recomposition.
 *
 * [shape] works like normal. [clip] defaults to `true` instead of
 * `elevation > 0.dp`, since that value isn't set until the lambda runs.
 *
 * The rest of the original parameters – `elevation`, `ambientColor`,
 * `spotColor`, `colorCompat`, and `forceColorCompat` – are now inside [block].
 *
 * **NB:** This should _not_ be used for shadows that extend beyond the root
 * composable's bounds on API levels 24..28. The clip feature requires a
 * compositing layer on those versions due to a bug in the system graphics, and
 * layers can be no larger than the root.
 *
 * Color compat, on any API level, has the same restriction, as its tint is
 * applied through a compositing layer.
 */
@Stable
public fun Modifier.clippedShadow(
    shape: Shape,
    clip: Boolean = true,
    block: ClippedShadowScope.() -> Unit
): Modifier {
    val modifier = this then BlockClippedShadowElement(shape, clip, block)
    return if (clip) modifier.clip(shape) else modifier
}

private class BlockClippedShadowElement(
    private val shape: Shape,
    private val clip: Boolean,
    private val block: ClippedShadowScope.() -> Unit
) : ModifierNodeElement<BlockClippedShadowNode>() {

    override fun create(): BlockClippedShadowNode =
        BlockClippedShadowNode(shape, block)

    override fun update(node: BlockClippedShadowNode) =
        node.update(shape, block)

    override fun InspectorInfo.inspectableProperties() {
        name = "clippedShadow"
        properties["shape"] = shape
        properties["clip"] = clip
        properties["block"] = block
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockClippedShadowElement) return false
        if (shape != other.shape) return false
        if (clip != other.clip) return false
        if (block !== other.block) return false
        return true
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + clip.hashCode()
        result = 31 * result + block.hashCode()
        return result
    }
}

private class BlockClippedShadowNode(
    shape: Shape,
    block: ClippedShadowScopeImpl.() -> Unit
) : BlockShadowNode<ClippedShadowScopeImpl>(shape, block) {

    override val isClipped: Boolean get() = true

    override val shadowScope: ClippedShadowScopeImpl = ClippedShadowScopeImpl()

    private val clip = Path()

    override fun onSetOutline(outline: Outline) =
        clip.run { rewind(); addOutline(outline) }

    override fun drawShadow(scope: DrawScope) =
        scope.clipPath(clip, ClipOp.Difference) { super.drawShadow(this) }
}

private class ClippedShadowScopeImpl :
    WorkingShadowScopeImpl(defaultColorCompat = DefaultShadowColor),
    ClippedShadowScope