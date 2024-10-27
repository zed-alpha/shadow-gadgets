package com.zedalpha.shadowgadgets.compose.internal

import android.graphics.Canvas
import android.graphics.Color.TRANSPARENT
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.core.graphics.withTranslation
import com.zedalpha.shadowgadgets.core.layer.Layer

@Composable
internal fun rememberComposeLayer(shadow: ComposeShadow): ComposeLayer {
    val view = LocalView.current
    val layer = remember(view, shadow) { ComposeLayer(view, shadow) }
    LaunchedEffect(view) { view.screenLocation.collect(layer::update) }
    DisposableEffect(layer) { onDispose { layer.dispose() } }
    return layer
}

internal class ComposeLayer(view: View, shadow: ComposeShadow) {

    private val coreLayer =
        Layer(view, TRANSPARENT, 0, 0).apply { addDraw(shadow) }

    var color: Color
        get() = Color(coreLayer.color)
        set(value) {
            val argb = value.toArgb()
            if (coreLayer.color == argb) return
            coreLayer.color = argb
        }

    private var screenLocation by mutableStateOf(IntOffset.Zero)

    fun update(location: IntOffset) {
        screenLocation = location
        coreLayer.recreate()
    }

    private var offset by mutableStateOf(Offset.Zero)

    fun setPosition(coordinates: LayoutCoordinates) {
        val rootSize = coordinates.findRootCoordinates().size
        coreLayer.setSize(rootSize.width, rootSize.height)
        offset = coordinates.positionInRoot()
    }

    fun refresh() = coreLayer.refresh()

    fun draw(canvas: Canvas) {
        screenLocation
        canvas.withTranslation(-offset.x, -offset.y, coreLayer::draw)
    }

    fun dispose() = coreLayer.dispose()
}