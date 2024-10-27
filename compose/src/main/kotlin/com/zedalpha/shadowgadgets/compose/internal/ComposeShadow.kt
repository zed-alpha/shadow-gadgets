package com.zedalpha.shadowgadgets.compose.internal

import android.graphics.Canvas
import android.view.View
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.isSimple
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.round
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.layer.LayerDraw
import android.graphics.Outline as AndroidOutline
import android.graphics.Path as AndroidPath

internal class ComposeShadow(view: View, clipped: Boolean) : LayerDraw {

    private val androidPath = AndroidPath()

    private val androidOutline = AndroidOutline().apply { alpha = 1.0F }

    private val coreShadow = if (clipped) {
        val provider = PathProvider { it.set(androidPath) }
        ClippedShadow(view).apply { pathProvider = provider }
    } else {
        Shadow(view)
    }

    fun setPosition(coordinates: LayoutCoordinates, isInLayer: Boolean) {
        val size = coordinates.size
        if (isInLayer) {
            val offset = coordinates.positionInRoot().round()
            coreShadow.setPosition(
                offset.x,
                offset.y,
                offset.x + size.width,
                offset.y + size.height
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

    private var tmpPath: Path? = null

    fun prepareDraw(
        elevation: Float,
        ambientColor: Color,
        spotColor: Color,
        isInLayer: Boolean
    ) {
        val shadow = coreShadow
        shadow.elevation = elevation
        if (isInLayer) {
            shadow.ambientColor = DefaultShadowColorInt
            shadow.spotColor = DefaultShadowColorInt
        } else {
            shadow.ambientColor = ambientColor.toArgb()
            shadow.spotColor = spotColor.toArgb()
        }
    }

    override fun draw(canvas: Canvas) = coreShadow.draw(canvas)

    fun dispose() = coreShadow.dispose()
}