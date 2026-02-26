package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import com.zedalpha.shadowgadgets.view.internal.OnLayoutChangeSizeAdapter
import com.zedalpha.shadowgadgets.view.internal.ThreadLocalGraphicsTemps

internal open class AutoPositionLayer(
    private val owner: View,
    private val layer: Layer,
    private val adjustBounds: ((Rect) -> Unit)? = null
) : Layer by layer {

    constructor(
        owner: View,
        content: (Canvas) -> Unit,
        adjustBounds: ((Rect) -> Unit)? = null
    ) : this(owner, Layer(owner, content), adjustBounds)

    private val sizeChange = OnLayoutChangeSizeAdapter(::updateBounds)

    private val rect = ThreadLocalGraphicsTemps.rect

    init {
        updateBounds(owner.width, owner.height)
        owner.addOnLayoutChangeListener(sizeChange)
    }

    final override fun dispose() {
        layer.dispose()
        owner.removeOnLayoutChangeListener(sizeChange)
    }

    private fun updateBounds(width: Int, height: Int) {
        val newBounds = rect
        newBounds.set(0, 0, width, height)
        adjustBounds?.invoke(newBounds)
        bounds = newBounds
    }
}