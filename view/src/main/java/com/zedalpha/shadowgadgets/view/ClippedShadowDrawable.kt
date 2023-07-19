package com.zedalpha.shadowgadgets.view

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.PathProvider
import kotlin.math.roundToInt

open class ClippedShadowDrawable private constructor(
    private val clippedShadow: ClippedShadow
) : Drawable() {

    constructor(ownerView: View) : this(ClippedShadow.newInstance(ownerView))

    @RequiresApi(29)
    constructor() : this(ClippedShadow.newInstance())

    fun setPathProvider(provider: ((Path) -> Unit)?) {
        clippedShadow.pathProvider =
            if (provider != null) PathProvider(provider) else null
    }

    override fun setAlpha(alpha: Int) {
        clippedShadow.alpha = alpha / 255F
    }

    override fun getAlpha(): Int {
        return (255 * clippedShadow.alpha).roundToInt()
    }

    var cameraDistance: Float
        get() = clippedShadow.cameraDistance
        set(value) {
            clippedShadow.cameraDistance = value
        }

    var elevation: Float
        get() = clippedShadow.elevation
        set(value) {
            clippedShadow.elevation = value
        }

    var pivotX: Float
        get() = clippedShadow.pivotX
        set(value) {
            clippedShadow.pivotX = value
        }

    var pivotY: Float
        get() = clippedShadow.pivotY
        set(value) {
            clippedShadow.pivotY = value
        }

    var rotationX: Float
        get() = clippedShadow.rotationX
        set(value) {
            clippedShadow.rotationX = value
        }

    var rotationY: Float
        get() = clippedShadow.rotationY
        set(value) {
            clippedShadow.rotationY = value
        }

    var rotationZ: Float
        get() = clippedShadow.rotationZ
        set(value) {
            clippedShadow.rotationZ = value
        }

    var scaleX: Float
        get() = clippedShadow.scaleX
        set(value) {
            clippedShadow.scaleX = value
        }

    var scaleY: Float
        get() = clippedShadow.scaleY
        set(value) {
            clippedShadow.scaleY = value
        }

    var translationX: Float
        get() = clippedShadow.translationX
        set(value) {
            clippedShadow.translationX = value
        }

    var translationY: Float
        get() = clippedShadow.translationY
        set(value) {
            clippedShadow.translationY = value
        }

    var translationZ: Float
        get() = clippedShadow.translationZ
        set(value) {
            clippedShadow.translationZ = value
        }

    var ambientColor: Int
        get() = clippedShadow.ambientColor
        set(value) {
            clippedShadow.ambientColor = value
        }

    var spotColor: Int
        get() = clippedShadow.spotColor
        set(value) {
            clippedShadow.spotColor = value
        }

    fun hasIdentityMatrix(): Boolean =
        clippedShadow.hasIdentityMatrix()

    fun getMatrix(outMatrix: Matrix) {
        clippedShadow.getMatrix(outMatrix)
    }

    fun setOutline(outline: Outline?) {
        clippedShadow.setOutline(outline)
    }

    override fun draw(canvas: Canvas) {
        clippedShadow.draw(canvas)
    }

    fun dispose() {
        clippedShadow.dispose()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}