package com.zedalpha.shadowgadgets.core.shadow

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper


internal class ViewShadow(ownerView: View) : CoreShadow() {

    private val shadowView = View(ownerView.context).apply {
        // Ensures draw when target is partially/fully out of bounds
        layout(0, 0, Int.MAX_VALUE, Int.MAX_VALUE)
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.set(this@ViewShadow.outline)
            }
        }
    }

    private var painter = ViewPainterProxy(ownerView)

    override fun dispose() {
        painter.dispose()
    }

    override var alpha: Float
        get() = shadowView.alpha
        set(value) {
            shadowView.alpha = value
        }

    override var cameraDistance: Float
        get() = shadowView.cameraDistance
        set(value) {
            shadowView.cameraDistance = value
        }

    override var elevation: Float
        get() = shadowView.elevation
        set(value) {
            shadowView.elevation = value
        }

    override var pivotX: Float
        get() = shadowView.pivotX
        set(value) {
            shadowView.pivotX = value
        }

    override var pivotY: Float
        get() = shadowView.pivotY
        set(value) {
            shadowView.pivotY = value
        }

    override var rotationX: Float
        get() = shadowView.rotationX
        set(value) {
            shadowView.rotationX = value
        }

    override var rotationY: Float
        get() = shadowView.rotationY
        set(value) {
            shadowView.rotationY = value
        }

    override var rotationZ: Float
        get() = shadowView.rotation
        set(value) {
            shadowView.rotation = value
        }

    override var scaleX: Float
        get() = shadowView.scaleX
        set(value) {
            shadowView.scaleX = value
        }

    override var scaleY: Float
        get() = shadowView.scaleY
        set(value) {
            shadowView.scaleY = value
        }

    override var translationX: Float
        get() = shadowView.translationX
        set(value) {
            shadowView.translationX = value
        }

    override var translationY: Float
        get() = shadowView.translationY
        set(value) {
            shadowView.translationY = value
        }

    override var translationZ: Float
        get() = shadowView.translationZ
        set(value) {
            shadowView.translationZ = value
        }

    override var ambientColor: Int
        get() = when {
            Build.VERSION.SDK_INT < 28 -> DefaultShadowColorInt
            else -> ViewShadowColorsHelper.getAmbientColor(shadowView)
        }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.setAmbientColor(shadowView, value)
            }
        }

    override var spotColor: Int
        get() = when {
            Build.VERSION.SDK_INT < 28 -> DefaultShadowColorInt
            else -> ViewShadowColorsHelper.getSpotColor(shadowView)
        }
        set(value) {
            if (Build.VERSION.SDK_INT >= 28) {
                ViewShadowColorsHelper.setSpotColor(shadowView, value)
            }
        }

    override val left: Int get() = shadowView.left

    override val top: Int get() = shadowView.top

    override val right: Int get() = shadowView.right

    override val bottom: Int get() = shadowView.bottom

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) {
        if (Build.VERSION.SDK_INT >= 29) {
            ViewPositionHelper.setPosition(shadowView, left, top, right, bottom)
        } else {
            shadowView.layout(left, top, right, bottom)
        }
    }

    override fun hasIdentityMatrix(): Boolean =
        shadowView.matrix.isIdentity

    override fun getMatrix(outMatrix: Matrix) {
        outMatrix.set(shadowView.matrix)
    }

    override fun onDraw(canvas: Canvas) {
        painter.drawShadowView(canvas, shadowView)
    }
}

@RequiresApi(29)
private object ViewPositionHelper {

    @DoNotInline
    fun setPosition(view: View, left: Int, top: Int, right: Int, bottom: Int) {
        view.setLeftTopRightBottom(left, top, right, bottom)
    }
}