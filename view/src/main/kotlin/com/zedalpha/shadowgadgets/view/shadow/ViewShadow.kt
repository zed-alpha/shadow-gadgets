package com.zedalpha.shadowgadgets.view.shadow

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import com.zedalpha.shadowgadgets.view.internal.BaseView
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.view.internal.fastLayout
import com.zedalpha.shadowgadgets.view.internal.viewPainter

internal class ViewShadow(link: View) : CoreShadow() {

    private val shadow = ShadowView(link.context)

    private val painter = link.viewPainter

    init {
        painter?.add(shadow)
    }

    override fun dispose() {
        painter?.remove(shadow)
    }

    override var alpha: Float
        get() = shadow.alpha
        set(value) {
            shadow.alpha = value
        }

    override var cameraDistance: Float
        get() = shadow.cameraDistance
        set(value) {
            shadow.cameraDistance = value
        }

    override var elevation: Float
        get() = shadow.elevation
        set(value) {
            shadow.elevation = value
        }

    override var pivotX: Float
        get() = shadow.pivotX
        set(value) {
            shadow.pivotX = value
        }

    override var pivotY: Float
        get() = shadow.pivotY
        set(value) {
            shadow.pivotY = value
        }

    override var rotationX: Float
        get() = shadow.rotationX
        set(value) {
            shadow.rotationX = value
        }

    override var rotationY: Float
        get() = shadow.rotationY
        set(value) {
            shadow.rotationY = value
        }

    override var rotationZ: Float
        get() = shadow.rotation
        set(value) {
            shadow.rotation = value
        }

    override var scaleX: Float
        get() = shadow.scaleX
        set(value) {
            shadow.scaleX = value
        }

    override var scaleY: Float
        get() = shadow.scaleY
        set(value) {
            shadow.scaleY = value
        }

    override var translationX: Float
        get() = shadow.translationX
        set(value) {
            shadow.translationX = value
        }

    override var translationY: Float
        get() = shadow.translationY
        set(value) {
            shadow.translationY = value
        }

    override var translationZ: Float
        get() = shadow.translationZ
        set(value) {
            shadow.translationZ = value
        }

    override var ambientColor: Int
        get() =
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.getAmbientColor(shadow)
            } else {
                DefaultShadowColor
            }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.setAmbientColor(shadow, value)
            }
        }

    override var spotColor: Int
        get() =
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.getSpotColor(shadow)
            } else {
                DefaultShadowColor
            }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.setSpotColor(shadow, value)
            }
        }

    override val left: Int get() = shadow.left

    override val top: Int get() = shadow.top

    override val right: Int get() = shadow.right

    override val bottom: Int get() = shadow.bottom

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        shadow.fastLayout(left, top, right, bottom)

    override fun setOutline(outline: Outline) = shadow.outline.set(outline)

    override fun hasIdentityMatrix(): Boolean = shadow.matrix.isIdentity

    override fun getMatrix(outMatrix: Matrix) = outMatrix.set(shadow.matrix)

    override fun getInverseMatrix(outMatrix: Matrix) {
        shadow.matrix.invert(outMatrix)
    }

    override fun onDraw(canvas: Canvas) {
        shadow.run { superInvalidateOutline(); painter?.drawView(canvas, this) }
    }
}

private class ShadowView(context: Context) : BaseView(context) {

    val outline = Outline()

    init {
        visibility = GONE
        outlineProvider =
            object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) =
                    outline.set(this@ShadowView.outline)
            }
    }

    @Deprecated("Library stop")
    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) = Unit
}