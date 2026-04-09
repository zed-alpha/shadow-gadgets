package com.zedalpha.shadowgadgets.compose

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.CacheDrawScope
import androidx.compose.ui.draw.DropShadowScope
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import com.zedalpha.shadowgadgets.compose.internal.ClippedDropShadowNode
import com.zedalpha.shadowgadgets.compose.internal.MutableDensity

/**
 * Creates a [dropShadow][androidx.compose.ui.draw.dropShadow] replacement with
 * the content area clipped out.
 *
 * Refer to [dropShadow][androidx.compose.ui.draw.dropShadow]'s docs for
 * parameter details.
 */
@Stable
public fun Modifier.clippedDropShadow(shape: Shape, shadow: Shadow): Modifier =
    this then SimpleClippedDropShadowElement(shape, shadow)

private class SimpleClippedDropShadowElement(
    private val shape: Shape,
    private val shadow: Shadow
) : ModifierNodeElement<SimpleClippedDropShadowNode>() {

    override fun create(): SimpleClippedDropShadowNode =
        SimpleClippedDropShadowNode(shape, shadow)

    override fun update(node: SimpleClippedDropShadowNode) =
        node.update(shape, shadow)

    override fun InspectorInfo.inspectableProperties() {
        name = "clippedDropShadow"
        properties["shape"] = shape
        properties["shadow"] = shadow
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as SimpleClippedDropShadowElement
        if (shape != other.shape) return false
        if (shadow != other.shadow) return false
        return true
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + shadow.hashCode()
        return result
    }
}

private class SimpleClippedDropShadowNode(
    shape: Shape,
    private var shadow: Shadow
) : ClippedDropShadowNode(shape) {

    override val density: MutableDensity = MutableDensity(1F, 1F)

    override val shadowMargin: Float
        get() = with(density) { shadow.run { radius + spread }.toPx() }

    override fun createPainter(scope: CacheDrawScope): Painter =
        scope.obtainShadowContext().createDropShadowPainter(shape, shadow)

    fun update(shape: Shape, shadow: Shadow) {
        if (this.shape != shape) {
            this.shape = shape
            invalidatePainter()
            invalidateClip()
        }
        if (this.shadow != shadow) {
            this.shadow = shadow
            invalidatePainter()
        }
        invalidateDrawCache()
    }
}

/**
 * Creates a [dropShadow][androidx.compose.ui.draw.dropShadow] replacement with
 * the content area clipped out.
 *
 * Refer to [dropShadow][androidx.compose.ui.draw.dropShadow]'s docs for
 * parameter details.
 */
@Stable
public fun Modifier.clippedDropShadow(
    shape: Shape,
    block: DropShadowScope.() -> Unit
): Modifier =
    this then BlockClippedDropShadowElement(shape, block)

private class BlockClippedDropShadowElement(
    private val shape: Shape,
    private val block: DropShadowScope.() -> Unit
) : ModifierNodeElement<BlockClippedDropShadowNode>() {

    override fun create(): BlockClippedDropShadowNode =
        BlockClippedDropShadowNode(shape, block)

    override fun update(node: BlockClippedDropShadowNode) =
        node.update(shape, block)

    override fun InspectorInfo.inspectableProperties() {
        name = "clippedDropShadow"
        properties["shape"] = shape
        properties["block"] = block
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as BlockClippedDropShadowElement
        if (shape != other.shape) return false
        if (block !== other.block) return false
        return true
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + block.hashCode()
        return result
    }
}

private class BlockClippedDropShadowNode(
    shape: Shape,
    private var block: DropShadowScope.() -> Unit
) : ClippedDropShadowNode(shape), ObserverModifierNode {

    private val shadowState = ShadowState()

    override val density: MutableDensity get() = shadowState

    override val shadowMargin: Float get() = shadowState.run { radius + spread }

    // Despite its comments, dropShadow creates a new Shadow & Painter if offset
    // changes; they're both immutable, so that's the only way to update. Since
    // there is no optimized case, it's pointless to check each property, as any
    // scope value change will need new objects. Just don't do unrelated state
    // reads within the shadow's block, but that's a universal recommendation.
    override fun createPainter(scope: CacheDrawScope): Painter {
        val state = shadowState
        state.reset()
        observeReads { state.block() }
        val shadow = state.toShadow()
        val shadowContext = scope.obtainShadowContext()
        return shadowContext.createDropShadowPainter(shape, shadow)
    }

    fun update(shape: Shape, block: DropShadowScope.() -> Unit) {
        if (this.shape != shape) {
            this.shape = shape
            invalidatePainter()
            invalidateClip()
        }
        if (this.block != block) {
            this.block = block
            invalidatePainter()
        }
        invalidateDrawCache()
    }

    override fun onObservedReadsChanged() {
        invalidatePainter()
        invalidateDrawCache()
    }
}

private class ShadowState : DropShadowScope, MutableDensity {

    override var density: Float = 1F
    override var fontScale: Float = 1F

    override var radius: Float = 0F
    override var spread: Float = 0F

    // This is bad DX in dropShadow. Color.Unspecified states
    // that it's treated as transparent outside of comparisons.
    override var color: Color = Color.Black
        set(value) {
            field = value.takeOrElse { Color.Black }
        }

    override var brush: Brush? = null
    override var alpha: Float = 1F
    override var blendMode: BlendMode = BlendMode.SrcOver
    override var offset: Offset = Offset.Zero

    fun reset() {
        radius = 0F
        spread = 0F
        color = Color.Black
        brush = null
        alpha = 1F
        blendMode = BlendMode.SrcOver
        offset = Offset.Zero
    }

    fun toShadow(): Shadow {
        val brush = this.brush
        return if (brush != null) {
            Shadow(radiusDp, brush, spreadDp, offsetDp, alpha, blendMode)
        } else {
            Shadow(radiusDp, color, spreadDp, offsetDp, alpha, blendMode)
        }
    }

    private inline val radiusDp: Dp get() = radius.toDp()
    private inline val spreadDp: Dp get() = spread.toDp()
    private inline val offsetDp: DpOffset
        get() = DpOffset(offset.x.toDp(), offset.y.toDp())
}