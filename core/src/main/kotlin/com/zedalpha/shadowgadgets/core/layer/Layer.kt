package com.zedalpha.shadowgadgets.core.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.view.View
import com.zedalpha.shadowgadgets.core.isNotDefault
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

public class Layer(layerDraw: LayerDraw, ownerView: View) {

    private val managedLayer: ManagedLayer =
        if (RenderNodeFactory.isOpen) {
            RenderNodeLayer(layerDraw)
        } else {
            ViewLayer(ownerView, layerDraw)
        }

    private val paint = Paint()

    private var width: Int = 0
    private var height: Int = 0

    public var color: Int = Color.TRANSPARENT
        set(value) {
            if (field == value) return
            field = value
            paint.setLayerFilter(color)
            managedLayer.setLayerPaint(paint)
        }

    init {
        setSize(ownerView.width, ownerView.height)
    }

    public fun setSize(width: Int, height: Int) {
        if (this.width == width && this.height == height) return
        this.width = width; this.height = height
        managedLayer.setSize(width, height)
    }

    public fun draw(canvas: Canvas) {
        if (canvas.isHardwareAccelerated) managedLayer.draw(canvas)
    }

    public fun invalidate(): Unit = managedLayer.invalidate()

    public fun refresh(): Unit = managedLayer.refresh()

    public fun recreate() {
        managedLayer.apply {
            recreate()
            setLayerPaint(paint)
            setSize(width, height)
        }
    }

    public fun dispose(): Unit = managedLayer.dispose()
}

public fun interface LayerDraw {
    public fun draw(canvas: Canvas)
}

public val RequiresDefaultClipLayer: Boolean = Build.VERSION.SDK_INT in 24..28

private fun Paint.setLayerFilter(color: Int) {
    if (color.isNotDefault) {
        alpha = Color.alpha(color)
        colorFilter = ColorMatrixColorFilter(
            floatArrayOf(
                0F, 0F, 0F, 0F, Color.red(color).toFloat(),
                0F, 0F, 0F, 0F, Color.green(color).toFloat(),
                0F, 0F, 0F, 0F, Color.blue(color).toFloat(),
                0F, 0F, 0F, 1F, 0F
            )
        )
    } else {
        alpha = 255
        colorFilter = null
    }
}