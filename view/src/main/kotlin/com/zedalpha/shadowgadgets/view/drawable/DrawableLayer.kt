package com.zedalpha.shadowgadgets.view.drawable

import android.graphics.Canvas
import android.view.View
import com.zedalpha.shadowgadgets.view.layer.IndividualLayer
import com.zedalpha.shadowgadgets.view.layer.Layer

internal class DrawableLayer(
    owner: View,
    content: (Canvas) -> Unit,
    invalidate: () -> Unit
) : IndividualLayer(
    owner = owner,
    invalidate = invalidate,
    layer = Layer(owner, content)
)