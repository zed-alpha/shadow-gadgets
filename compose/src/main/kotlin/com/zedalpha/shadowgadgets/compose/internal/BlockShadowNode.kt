package com.zedalpha.shadowgadgets.compose.internal

import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.observeReads

internal abstract class BlockShadowNode<T : WorkingShadowScopeImpl>(
    shape: Shape,
    var block: T.() -> Unit
) : ShadowNode(shape), ObserverModifierNode {

    private var currentBlock: (T.() -> Unit)? = null

    final override fun onAttach() {
        super.onAttach()
        currentBlock = null
    }

    fun update(shape: Shape, block: T.() -> Unit) {
        if (this.shape != shape) invalidateDrawCache() else invalidateDraw()
        this.shape = shape
        this.block = block
    }

    abstract override val shadowScope: T

    final override fun onDraw(scope: DrawScope) {
        val nextBlock = block
        if (currentBlock !== nextBlock) {
            val shadowScope = this.shadowScope
            shadowScope.reset()
            observeReads { shadowScope.nextBlock() }
            currentBlock = nextBlock
            updateShadow()
        }

        super.onDraw(scope)
    }

    final override fun onObservedReadsChanged() {
        currentBlock = null
        invalidateDraw()
    }

    final override fun onDensityInvalidated() {
        currentBlock = null
    }
}