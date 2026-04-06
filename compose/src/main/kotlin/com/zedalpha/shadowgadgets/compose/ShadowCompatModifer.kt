package com.zedalpha.shadowgadgets.compose

import android.os.Build
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.DefaultShadowColor
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.zedalpha.shadowgadgets.compose.internal.BlockShadowNode
import com.zedalpha.shadowgadgets.compose.internal.SimpleShadowNode
import com.zedalpha.shadowgadgets.compose.internal.WorkingShadowScopeImpl
import com.zedalpha.shadowgadgets.compose.internal.isOrBlendsToDefault

/**
 * Creates a [shadow] replacement that can be tinted with the library's color
 * compat mechanism on API levels before 28, the earliest version to support the
 * native shadow colors. If the current API level is 28 or above, [shadowCompat]
 * falls back to the framework's [shadow], unless [forceColorCompat] is `true`.
 *
 * Refer to [shadow]'s docs for details on the first five parameters:
 * [elevation], [shape], [clip], [ambientColor], and [spotColor].
 *
 * [colorCompat] takes a [Color] that's used to tint the shadow on API levels
 * 27 and below. If the passed value is [Color.Black] – the default shadow color
 * – this falls back to the normal [shadow]. If [Color.Unspecified] is passed,
 * the actual tint is calculated as a blend of the [ambientColor] and
 * [spotColor], mixed in proportion to their current theme alphas. Setting any
 * other value disables this blending behavior.
 *
 * The color blending formula gives good results only if the ambient and spot
 * colors are both fully opaque; i.e., only if both have maximum alpha values.
 *
 * **NB:** This should not be used for shadows that extend beyond the root
 * composable's bounds. Color compat applies its tint through a compositing
 * layer, which can be no larger than the root.
 */
@Stable
public fun Modifier.shadowCompat(
    elevation: Dp,
    shape: Shape = RectangleShape,
    clip: Boolean = elevation > 0.dp,
    ambientColor: Color = DefaultShadowColor,
    spotColor: Color = DefaultShadowColor,
    colorCompat: Color = Color.Unspecified,
    forceColorCompat: Boolean = false
): Modifier {
    var doClip = clip

    val modifier =
        if (elevation > 0.dp) {
            if (Build.VERSION.SDK_INT >= 28 && !forceColorCompat ||
                colorCompat.isOrBlendsToDefault(ambientColor, spotColor)
            ) {
                doClip = false

                this.shadow(
                    elevation = elevation,
                    shape = shape,
                    clip = clip,
                    ambientColor = ambientColor,
                    spotColor = spotColor
                )
            } else {
                this then ShadowCompatElement(
                    elevation = elevation,
                    shape = shape,
                    clip = clip,
                    ambientColor = ambientColor,
                    spotColor = spotColor,
                    colorCompat = colorCompat,
                    forceColorCompat = forceColorCompat
                )
            }
        } else {
            this
        }

    return if (doClip) modifier.clip(shape) else modifier
}

private class ShadowCompatElement(
    private val elevation: Dp,
    private val shape: Shape,
    private val clip: Boolean,
    private val ambientColor: Color,
    private val spotColor: Color,
    private val colorCompat: Color,
    private val forceColorCompat: Boolean
) : ModifierNodeElement<SimpleShadowCompatNode>() {

    override fun create(): SimpleShadowCompatNode =
        SimpleShadowCompatNode(
            elevation = elevation,
            shape = shape,
            ambientColor = ambientColor,
            spotColor = spotColor,
            colorCompat = colorCompat,
            forceColorCompat = forceColorCompat
        )

    override fun update(node: SimpleShadowCompatNode) {
        node.elevationDp = elevation
        node.ambientColor = ambientColor
        node.spotColor = spotColor
        node.colorCompat = colorCompat
        node.forceColorCompat = forceColorCompat
        node.update(shape)
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shadowCompat"
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
        if (other !is ShadowCompatElement) return false
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

private class SimpleShadowCompatNode(
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
    override val isClipped: Boolean get() = false
}

/**
 * The specific scope for the lambda overload of [shadowCompat].
 */
public interface ShadowCompatScope : ShadowGadgetsScope

/**
 * The lambda version of [shadowCompat] that allows for modification of shadow
 * properties without recomposition.
 *
 * [shape] works like normal. [clip] defaults to `true` instead of
 * `elevation > 0.dp`, since that value isn't set until the lambda runs.
 *
 * The rest of the original parameters – `elevation`, `ambientColor`,
 * `spotColor`, `colorCompat`, and `forceColorCompat` – are now inside [block].
 *
 * Unlike the original, this version never falls back to the framework's
 * [shadow], but it does disable internal layer compositing whenever possible.
 *
 * **NB:** This should not be used for shadows that extend beyond the root
 * composable's bounds. Color compat applies its tint through a compositing
 * layer, which can be no larger than the root.
 */
@Stable
public fun Modifier.shadowCompat(
    shape: Shape,
    clip: Boolean = true,
    block: ShadowCompatScope.() -> Unit
): Modifier {
    val modifier = this then BlockShadowCompatElement(shape, clip, block)
    return if (clip) modifier.clip(shape) else modifier
}

private class BlockShadowCompatElement(
    private val shape: Shape,
    private val clip: Boolean,
    private val block: ShadowCompatScope.() -> Unit
) : ModifierNodeElement<BlockShadowCompatNode>() {

    override fun create(): BlockShadowCompatNode =
        BlockShadowCompatNode(shape, block)

    override fun update(node: BlockShadowCompatNode) =
        node.update(shape, block)

    override fun InspectorInfo.inspectableProperties() {
        name = "shadowCompat"
        properties["shape"] = shape
        properties["clip"] = clip
        properties["block"] = block
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockShadowCompatElement) return false
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

private class BlockShadowCompatNode(
    shape: Shape,
    block: ShadowCompatScopeImpl.() -> Unit
) : BlockShadowNode<ShadowCompatScopeImpl>(shape, block) {

    override val isClipped: Boolean get() = false

    override val shadowScope: ShadowCompatScopeImpl = ShadowCompatScopeImpl()
}

private class ShadowCompatScopeImpl :
    WorkingShadowScopeImpl(defaultColorCompat = Color.Unspecified),
    ShadowCompatScope