package com.zedalpha.shadowgadgets.core.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.os.Build
import android.view.View
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.fastForEach
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory

public sealed class Layer(
    ownerView: View,
    color: Int,
    private var width: Int,
    private var height: Int
) {
    private val layer: ManagedLayer =
        if (RenderNodeFactory.isOpen) {
            RenderNodeLayer(::contentDraw)
        } else {
            ViewLayer(ownerView, ::contentDraw)
        }

    protected abstract fun contentDraw(canvas: Canvas)

    private val paint = Paint()

    public var color: Int = Color.TRANSPARENT
        set(value) {
            if (field == value) return
            field = value
            paint.setLayerFilter(color)
            layer.setLayerPaint(paint)
        }

    init {
        layer.setSize(width, height)
        this.color = color
    }

    public fun setSize(width: Int, height: Int) {
        if (this.width == width && this.height == height) return
        this.width = width; this.height = height
        layer.setSize(width, height)
    }

    public fun draw(canvas: Canvas) {
        if (canvas.isHardwareAccelerated) layer.draw(canvas)
    }

    public fun invalidate(): Unit = layer.invalidate()

    public fun refresh(): Unit = layer.refresh()

    public fun recreate() {
        layer.apply {
            recreate()
            setLayerPaint(paint)
            setSize(width, height)
        }
    }

    @CallSuper
    public open fun dispose(): Unit = layer.dispose()
}

public class SingleDrawLayer(
    ownerView: View,
    color: Int,
    width: Int,
    height: Int,
    private val layerDraw: LayerDraw
) : Layer(ownerView, color, width, height) {

    override fun contentDraw(canvas: Canvas): Unit = layerDraw.draw(canvas)
}

public class MultiDrawLayer(
    ownerView: View,
    color: Int,
    width: Int,
    height: Int
) : Layer(ownerView, color, width, height) {

    private val layerDraws = mutableListOf<LayerDraw>()

    public fun addDraw(layerDraw: LayerDraw) {
        layerDraws.add(layerDraw)
    }

    public fun removeDraw(layerDraw: LayerDraw) {
        layerDraws.remove(layerDraw)
    }

    public fun isEmpty(): Boolean = layerDraws.isEmpty()

    override fun contentDraw(canvas: Canvas): Unit =
        layerDraws.fastForEach { it.draw(canvas) }

    override fun dispose() {
        super.dispose()
        layerDraws.clear()
    }
}

public fun interface LayerDraw {
    public fun draw(canvas: Canvas)
}

public val RequiresDefaultClipLayer: Boolean = Build.VERSION.SDK_INT in 24..28

private fun Paint.setLayerFilter(color: Int) {
    if (color != DefaultShadowColorInt) {
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