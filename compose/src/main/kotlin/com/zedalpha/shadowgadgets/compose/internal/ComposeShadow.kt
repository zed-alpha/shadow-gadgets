package com.zedalpha.shadowgadgets.compose.internal

import android.graphics.Canvas
import android.graphics.Color.TRANSPARENT
import android.view.View
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSimple
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import androidx.core.graphics.withTranslation
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.layer.SingleDrawLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import android.graphics.Outline as AndroidOutline
import android.graphics.Path as AndroidPath

internal class ComposeShadow(view: View, clipped: Boolean, useLayer: Boolean) {

    private val androidPath = AndroidPath()

    private val androidOutline = AndroidOutline().apply { alpha = 1.0F }

    private val coreShadow = if (clipped) {
        val provider = PathProvider { it.set(androidPath) }
        ClippedShadow(view).apply { pathProvider = provider }
    } else {
        Shadow(view)
    }

    private var coreLayer: SingleDrawLayer? = null

    private var locationScope: CoroutineScope? = null

    private var screenLocation by mutableStateOf(IntOffset.Zero)

    init {
        if (useLayer) {
            val layer =
                SingleDrawLayer(view, TRANSPARENT, 0, 0, coreShadow::draw)
                    .also { coreLayer = it }
            val scope =
                CoroutineScope(Dispatchers.Default)
                    .also { locationScope = it }
            scope.launch {
                view.screenLocation.collect { location ->
                    screenLocation = location
                    layer.recreate()
                }
            }
        }
    }

    private var layerOffset by mutableStateOf(Offset.Zero)

    fun setPosition(coordinates: LayoutCoordinates) {
        val layer = coreLayer
        val size = coordinates.size

        if (layer != null) {
            val positionInRoot = coordinates.positionInRoot()

            val rootSize = coordinates.findRootCoordinates().size
            layer.setSize(rootSize.width, rootSize.height)
            layerOffset = positionInRoot

            val position = positionInRoot.round()
            coreShadow.setPosition(
                position.x,
                position.y,
                position.x + size.width,
                position.y + size.height
            )
        } else {
            coreShadow.setPosition(0, 0, size.width, size.height)
        }
    }

    fun setShape(
        shape: Shape,
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ) {
        val outline = shape.createOutline(size, layoutDirection, density)
        outline.applyTo(androidOutline, androidPath)
        coreShadow.setOutline(androidOutline)
    }

    private var tmpPath: Path? = null

    private fun Outline.applyTo(
        androidOutline: AndroidOutline,
        androidPath: AndroidPath
    ) {
        androidPath.reset()
        when (this) {
            is Outline.Rectangle -> {
                setRectangle(rect, androidOutline, androidPath)
            }
            is Outline.Rounded -> if (roundRect.isSimple) {
                setRoundedSimple(roundRect, androidOutline, androidPath)
            } else {
                val tmp = tmpPath ?: Path().also { tmpPath = it }
                setRoundedComplex(roundRect, androidOutline, androidPath, tmp)
            }
            is Outline.Generic -> {
                setGeneric(path, androidOutline, androidPath)
            }
        }
    }

    fun draw(
        canvas: Canvas,
        elevation: Float,
        ambientColor: Color,
        spotColor: Color,
        colorCompat: Color
    ) {
        val shadow = coreShadow
        shadow.elevation = elevation

        val layer = coreLayer
        if (layer != null) {
            shadow.ambientColor = DefaultShadowColorInt
            shadow.spotColor = DefaultShadowColorInt
            layer.color = colorCompat.toArgb()
            layer.refresh()

            screenLocation
            val offset = layerOffset
            canvas.withTranslation(-offset.x, -offset.y, layer::draw)
        } else {
            shadow.ambientColor = ambientColor.toArgb()
            shadow.spotColor = spotColor.toArgb()

            coreShadow.draw(canvas)
        }
    }

    fun dispose() {
        locationScope?.cancel()
        coreLayer?.dispose()
        coreShadow.dispose()
    }
}