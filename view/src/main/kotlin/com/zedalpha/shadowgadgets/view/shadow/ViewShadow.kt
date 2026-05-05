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
import com.zedalpha.shadowgadgets.view.internal.obtainViewPainter

internal class ViewShadow(link: View) : CoreShadow() {

    private val view = ShadowView(link.context)

    private val painter = link.obtainViewPainter()

    init {
        painter?.add(view)
    }

    override fun dispose() {
        painter?.remove(view)
    }

    override var alpha: Float
        get() = view.alpha
        set(value) {
            view.alpha = value
        }

    override var cameraDistance: Float
        get() = view.cameraDistance
        set(value) {
            view.cameraDistance = value
        }

    override var elevation: Float
        get() = view.elevation
        set(value) {
            view.elevation = value
        }

    override var pivotX: Float
        get() = view.pivotX
        set(value) {
            view.pivotX = value
        }

    override var pivotY: Float
        get() = view.pivotY
        set(value) {
            view.pivotY = value
        }

    override var rotationX: Float
        get() = view.rotationX
        set(value) {
            view.rotationX = value
        }

    override var rotationY: Float
        get() = view.rotationY
        set(value) {
            view.rotationY = value
        }

    override var rotationZ: Float
        get() = view.rotation
        set(value) {
            view.rotation = value
        }

    override var scaleX: Float
        get() = view.scaleX
        set(value) {
            view.scaleX = value
        }

    override var scaleY: Float
        get() = view.scaleY
        set(value) {
            view.scaleY = value
        }

    override var translationX: Float
        get() = view.translationX
        set(value) {
            view.translationX = value
        }

    override var translationY: Float
        get() = view.translationY
        set(value) {
            view.translationY = value
        }

    override var translationZ: Float
        get() = view.translationZ
        set(value) {
            view.translationZ = value
        }

    override var ambientColor: Int
        get() =
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.getAmbientColor(view)
            } else {
                DefaultShadowColor
            }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.setAmbientColor(view, value)
            }
        }

    override var spotColor: Int
        get() =
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.getSpotColor(view)
            } else {
                DefaultShadowColor
            }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.setSpotColor(view, value)
            }
        }

    override val left: Int get() = view.left

    override val top: Int get() = view.top

    override val right: Int get() = view.right

    override val bottom: Int get() = view.bottom

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        view.fastLayout(left, top, right, bottom)

    override fun setOutline(outline: Outline) = view.setOutline(outline)

    override fun hasIdentityMatrix(): Boolean = view.matrix.isIdentity

    override fun getMatrix(outMatrix: Matrix) = outMatrix.set(view.matrix)

    override fun getInverseMatrix(outMatrix: Matrix) {
        view.matrix.invert(outMatrix)
    }

    override fun onDraw(canvas: Canvas) {
        painter?.drawView(canvas, view)
    }
}

private class ShadowView(context: Context) : BaseView(context) {

    private val outline = Outline()

    init {
        visibility = GONE
        outlineProvider =
            object : ViewOutlineProvider() {
                override fun getOutline(view: View, outline: Outline) =
                    outline.set(this@ShadowView.outline)
            }
    }

    fun setOutline(outline: Outline) {
        this.outline.set(outline)
        superInvalidateOutline()
    }

    @Deprecated("Library stop")
    @SuppressLint("MissingSuperCall")
    override fun draw(canvas: Canvas) = Unit
}