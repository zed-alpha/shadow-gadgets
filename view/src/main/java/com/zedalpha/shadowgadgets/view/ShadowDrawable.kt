package com.zedalpha.shadowgadgets.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.ShadowColorFilter
import kotlin.math.roundToInt

class ShadowDrawable private constructor(
    private val shadow: Shadow
) : Drawable() {

    constructor(ownerView: View) : this(Shadow(ownerView))

    @RequiresApi(29)
    constructor() : this(Shadow())

    private var colorFilter: ShadowColorFilter? = null

    @get:ColorInt
    @setparam:ColorInt
    var color: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value
            if (value != DefaultShadowColorInt) {
                val filter =
                    colorFilter ?: ShadowColorFilter().also { colorFilter = it }
                filter.color = value
            } else {
                colorFilter = null
            }
        }

    override fun setAlpha(alpha: Int) {
        shadow.alpha = alpha / 255F
    }

    override fun getAlpha(): Int {
        return (255 * shadow.alpha).roundToInt()
    }

    var cameraDistance: Float
        get() = shadow.cameraDistance
        set(value) {
            shadow.cameraDistance = value
        }

    var elevation: Float
        get() = shadow.elevation
        set(value) {
            shadow.elevation = value
        }

    var pivotX: Float
        get() = shadow.pivotX
        set(value) {
            shadow.pivotX = value
        }

    var pivotY: Float
        get() = shadow.pivotY
        set(value) {
            shadow.pivotY = value
        }

    var rotationX: Float
        get() = shadow.rotationX
        set(value) {
            shadow.rotationX = value
        }

    var rotationY: Float
        get() = shadow.rotationY
        set(value) {
            shadow.rotationY = value
        }

    var rotationZ: Float
        get() = shadow.rotationZ
        set(value) {
            shadow.rotationZ = value
        }

    var scaleX: Float
        get() = shadow.scaleX
        set(value) {
            shadow.scaleX = value
        }

    var scaleY: Float
        get() = shadow.scaleY
        set(value) {
            shadow.scaleY = value
        }

    var translationX: Float
        get() = shadow.translationX
        set(value) {
            shadow.translationX = value
        }

    var translationY: Float
        get() = shadow.translationY
        set(value) {
            shadow.translationY = value
        }

    var translationZ: Float
        get() = shadow.translationZ
        set(value) {
            shadow.translationZ = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var ambientColor: Int
        get() = shadow.ambientColor
        set(value) {
            shadow.ambientColor = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var spotColor: Int
        get() = shadow.spotColor
        set(value) {
            shadow.spotColor = value
        }

    fun hasIdentityMatrix(): Boolean =
        shadow.hasIdentityMatrix()

    fun getMatrix(outMatrix: Matrix) {
        shadow.getMatrix(outMatrix)
    }

    fun setOutline(outline: Outline?) {
        shadow.setOutline(outline)
    }

    override fun draw(canvas: Canvas) {
        when (val filter = colorFilter) {
            null -> shadow.draw(canvas)
            else -> filter.draw(canvas, shadow)
        }
    }

    fun dispose() {
        shadow.dispose()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}