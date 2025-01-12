package com.zedalpha.shadowgadgets.core.rendernode

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Paint
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

public sealed interface RenderNodeWrapper {

    @get:FloatRange(from = 0.0, to = 1.0)
    @setparam:FloatRange(from = 0.0, to = 1.0)
    public var alpha: Float

    public var cameraDistance: Float

    public var elevation: Float

    public var pivotX: Float

    public var pivotY: Float

    public var rotationX: Float

    public var rotationY: Float

    public var rotationZ: Float

    public var scaleX: Float

    public var scaleY: Float

    public var translationX: Float

    public var translationY: Float

    public var translationZ: Float

    @get:ColorInt
    @setparam:ColorInt
    public var ambientColor: Int

    @get:ColorInt
    @setparam:ColorInt
    public var spotColor: Int

    public val left: Int

    public val top: Int

    public val right: Int

    public val bottom: Int

    public fun setPosition(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ): Boolean

    public fun setOutline(outline: Outline?)

    public fun hasIdentityMatrix(): Boolean

    public fun getMatrix(outMatrix: Matrix)

    public fun draw(canvas: Canvas)

    public fun setClipToBounds(clipToBounds: Boolean): Boolean

    public fun setProjectBackwards(shouldProject: Boolean): Boolean

    public fun setProjectionReceiver(shouldReceive: Boolean): Boolean

    public fun beginRecording(width: Int, height: Int): Canvas

    public fun endRecording(canvas: Canvas)

    public fun hasDisplayList(): Boolean

    public fun discardDisplayList()

    public fun setUseCompositingLayer(
        forceToLayer: Boolean,
        paint: Paint?
    ): Boolean
}