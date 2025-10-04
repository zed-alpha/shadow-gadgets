package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.view.View

internal class DrawableLayer(
    owner: View,
    content: (Canvas) -> Unit,
    invalidate: () -> Unit
) : IndividualLayer(
    owner = owner,
    invalidate = invalidate,
    layer = Layer(owner, content)
)