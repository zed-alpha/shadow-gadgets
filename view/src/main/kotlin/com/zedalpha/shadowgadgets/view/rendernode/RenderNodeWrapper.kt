package com.zedalpha.shadowgadgets.view.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

internal sealed interface RenderNodeWrapper {

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

    val left: Int

    val top: Int

    val right: Int

    val bottom: Int

    fun setPosition(left: Int, top: Int, right: Int, bottom: Int): Boolean

    fun setOutline(outline: Outline?)

    fun hasIdentityMatrix(): Boolean

    fun getMatrix(outMatrix: Matrix)

    fun getInverseMatrix(outMatrix: Matrix)

    fun drawRenderNode(canvas: Canvas)

    fun setClipToBounds(clipToBounds: Boolean): Boolean

    fun setProjectBackwards(shouldProject: Boolean): Boolean

    fun setProjectionReceiver(shouldReceive: Boolean): Boolean

    fun beginRecording(): Canvas

    fun endRecording()

    fun hasDisplayList(): Boolean

    fun discardDisplayList()

    fun setUseCompositingLayer(forceToLayer: Boolean, paint: Paint?): Boolean

    fun setHasOverlappingRendering(hasOverlappingRendering: Boolean): Boolean
}