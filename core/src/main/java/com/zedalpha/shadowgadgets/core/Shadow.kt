package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

interface Shadow {

    @get:FloatRange(from = 0.0, to = 1.0)
    @setparam:FloatRange(from = 0.0, to = 1.0)
    var alpha: Float

    var cameraDistance: Float

    var elevation: Float

    var pivotX: Float

    var pivotY: Float

    var rotationX: Float

    var rotationY: Float

    var rotationZ: Float

    var scaleX: Float

    var scaleY: Float

    var translationX: Float

    var translationY: Float

    var translationZ: Float

    @get:ColorInt
    @setparam:ColorInt
    var ambientColor: Int

    @get:ColorInt
    @setparam:ColorInt
    var spotColor: Int

    fun hasIdentityMatrix(): Boolean

    fun getMatrix(outMatrix: Matrix)

    fun setOutline(outline: Outline?)

    fun draw(canvas: Canvas)
}