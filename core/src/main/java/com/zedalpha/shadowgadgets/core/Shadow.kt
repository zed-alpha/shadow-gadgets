package com.zedalpha.shadowgadgets.core

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.rendernode.RenderNodeFactory


fun Shadow(
    ownerView: View,
    forceViewType: Boolean = false
): Shadow = if (RenderNodeFactory.isOpenForBusiness && !forceViewType) {
    RenderNodeShadow()
} else {
    ViewShadow(ownerView)
}

@RequiresApi(29)
fun Shadow(): Shadow = RenderNodeShadow()

internal sealed class CoreShadow : Shadow {

    override val outline = Outline()

    @CallSuper
    override fun setOutline(outline: Outline?) {
        if (outline == null) {
            this.outline.setEmpty()
        } else {
            this.outline.set(outline)
        }
    }

    override fun draw(canvas: Canvas) {
        if (canvas.isHardwareAccelerated) {
            enableZ(canvas)
            onDraw(canvas)
            disableZ(canvas)
        }
    }

    protected abstract fun onDraw(canvas: Canvas)
}

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

    val outline: Outline

    fun setOutline(outline: Outline?)

    fun hasIdentityMatrix(): Boolean

    fun getMatrix(outMatrix: Matrix)

    fun draw(canvas: Canvas)

    fun dispose()
}