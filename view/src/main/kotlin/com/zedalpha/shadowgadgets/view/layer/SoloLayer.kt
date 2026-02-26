package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View

internal class SoloLayer(
    owner: View,
    content: (Canvas) -> Unit,
    invalidate: () -> Unit,
    adjustBounds: ((Rect) -> Unit)? = null
) : IndividualLayer(
    owner = owner,
    invalidate = invalidate,
    layer = AutoPositionLayer(owner, content, adjustBounds)
)