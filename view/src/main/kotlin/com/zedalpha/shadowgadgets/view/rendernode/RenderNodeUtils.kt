package com.zedalpha.shadowgadgets.view.rendernode

import android.graphics.Canvas
import android.graphics.RenderNode
import androidx.annotation.RequiresApi

internal inline val RenderNodeWrapper.width: Int get() = this.right - this.left
internal inline val RenderNodeWrapper.height: Int get() = this.bottom - this.top

internal inline fun RenderNodeWrapper.record(block: (Canvas) -> Unit) {
    val canvas = this.beginRecording()
    try {
        block(canvas)
    } finally {
        this.endRecording()
    }
}

@RequiresApi(29)
internal fun nativeRenderNode(name: String? = null): RenderNode =
    RenderNode(name).apply { clipToBounds = false }

@RequiresApi(29)
internal inline fun RenderNode.record(block: (Canvas) -> Unit) {
    val canvas = this.beginRecording()
    try {
        block(canvas)
    } finally {
        this.endRecording()
    }
}