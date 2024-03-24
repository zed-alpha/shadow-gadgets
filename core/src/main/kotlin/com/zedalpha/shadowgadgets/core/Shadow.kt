package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory
import com.zedalpha.shadowgadgets.core.shadow.RenderNodeShadow
import com.zedalpha.shadowgadgets.core.shadow.ViewShadow

fun Shadow(
    ownerView: View,
    forceViewType: Boolean = false
): Shadow = if (RenderNodeFactory.isOpen && !forceViewType) {
    RenderNodeShadow()
} else {
    ViewShadow(ownerView)
}

@RequiresApi(29)
fun Shadow(): Shadow = RenderNodeShadow()

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

    val left: Int

    val top: Int

    val right: Int

    val bottom: Int

    fun setPosition(left: Int, top: Int, right: Int, bottom: Int)

    val outline: Outline

    fun setOutline(outline: Outline)

    fun hasIdentityMatrix(): Boolean

    fun getMatrix(outMatrix: Matrix)

    fun draw(canvas: Canvas)

    fun dispose()
}