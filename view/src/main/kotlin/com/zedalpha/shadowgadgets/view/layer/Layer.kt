package com.zedalpha.shadowgadgets.view.layer

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.view.View
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.isDefault
import com.zedalpha.shadowgadgets.view.internal.isNotTint
import com.zedalpha.shadowgadgets.view.internal.isTint
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.proxy.ShadowProxy
import com.zedalpha.shadowgadgets.view.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.view.tintOutlineShadow

internal interface Layer {
    var color: Int
    var bounds: Rect
    fun draw(canvas: Canvas)
    fun recreate(): Boolean
    fun dispose()
}

internal fun Layer(link: View, content: (Canvas) -> Unit): Layer =
    if (RenderNodeFactory.isOpen) {
        RenderNodeLayer(link, content)
    } else {
        ViewLayer(link, content)
    }

internal abstract class AbstractLayer(
    protected val link: View,
    protected val content: (Canvas) -> Unit
) : Layer {

    protected val paint = Paint()

    protected var isOffscreen: Boolean = false
        private set

    final override var color: Int = DefaultShadowColor
        set(color) {
            if (field == color) return
            field = color
            paint.setLayerFilter(color)
            isOffscreen = color.isTint
            updateLayerType()
        }

    protected abstract fun updateLayerType()

    final override var bounds = Rect()
        set(value) {
            field.set(value)
            updateLayerBounds()
        }

    protected abstract fun updateLayerBounds()

    final override fun draw(canvas: Canvas) {
        if (canvas.isHardwareAccelerated) drawLayer(canvas)
    }

    protected abstract fun drawLayer(canvas: Canvas)

    final override fun recreate(): Boolean {
        if (color.isNotTint) return false
        recreateLayer()
        updateLayerType()
        updateLayerBounds()
        return true
    }

    protected abstract fun recreateLayer()
}

private fun Paint.setLayerFilter(color: Int) =
    if (color.isTint) {
        this.alpha = Color.alpha(color)
        this.colorFilter =
            ColorMatrixColorFilter(
                floatArrayOf(
                    0F, 0F, 0F, 0F, Color.red(color).toFloat(),
                    0F, 0F, 0F, 0F, Color.green(color).toFloat(),
                    0F, 0F, 0F, 0F, Color.blue(color).toFloat(),
                    0F, 0F, 0F, 1F, 0F
                )
            )
    } else {
        this.alpha = if (color.isDefault) 255 else 0
        this.colorFilter = null
    }

internal val RequiresDefaultClipLayer = Build.VERSION.SDK_INT in 24..28

internal val ShadowProxy.desiredLayerColor: Int?
    get() =
        when {
            this.target.tintOutlineShadow -> this.target.outlineShadowColorCompat
            RequiresDefaultClipLayer -> DefaultShadowColor
            else -> null
        }