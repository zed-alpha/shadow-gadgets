@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.rendernode

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.shadow.ShadowColorsHelper
import java.lang.reflect.Method


internal interface RenderNodeWrapper {
    fun getAlpha(): Float
    fun setAlpha(alpha: Float): Boolean

    fun getCameraDistance(): Float
    fun setCameraDistance(distance: Float): Boolean

    fun getElevation(): Float
    fun setElevation(elevation: Float): Boolean

    fun getPivotX(): Float
    fun setPivotX(pivotX: Float): Boolean

    fun getPivotY(): Float
    fun setPivotY(pivotY: Float): Boolean

    fun getRotationX(): Float
    fun setRotationX(rotationX: Float): Boolean

    fun getRotationY(): Float
    fun setRotationY(rotationY: Float): Boolean

    fun getRotationZ(): Float
    fun setRotationZ(rotationZ: Float): Boolean

    fun getScaleX(): Float
    fun setScaleX(scaleX: Float): Boolean

    fun getScaleY(): Float
    fun setScaleY(scaleY: Float): Boolean

    fun getTranslationX(): Float
    fun setTranslationX(translationX: Float): Boolean

    fun getTranslationY(): Float
    fun setTranslationY(translationY: Float): Boolean

    fun getTranslationZ(): Float
    fun setTranslationZ(translationZ: Float): Boolean

    fun setOutline(outline: Outline?): Boolean
    fun setPosition(left: Int, top: Int, right: Int, bottom: Int): Boolean

    fun hasIdentityMatrix(): Boolean
    fun getMatrix(outMatrix: Matrix)

    fun setProjectBackwards(shouldProject: Boolean): Boolean
    fun setProjectionReceiver(shouldReceive: Boolean): Boolean

    fun beginRecording(width: Int, height: Int): Canvas
    fun endRecording(canvas: Canvas)

    fun setClipToBounds(clipToBounds: Boolean): Boolean
    fun hasDisplayList(): Boolean

    fun draw(canvas: Canvas)
}

@RequiresApi(Build.VERSION_CODES.P)
internal interface RenderNodeColors {
    @ColorInt
    fun getAmbientShadowColor(): Int
    fun setAmbientShadowColor(@ColorInt color: Int): Boolean

    @ColorInt
    fun getSpotShadowColor(): Int
    fun setSpotShadowColor(@ColorInt color: Int): Boolean
}

internal object RenderNodeFactory {
    val isOpenForBusiness = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
            (Build.VERSION.SDK_INT != Build.VERSION_CODES.P && testHiddenApi())

    @SuppressLint("NewApi")
    fun newInstance() = when (Build.VERSION.SDK_INT) {
        Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1 -> RenderNodeApi21()
        in Build.VERSION_CODES.M..Build.VERSION_CODES.P -> RenderNodeApi23()
        Build.VERSION_CODES.P -> RenderNodeApi28() // Currently kinda pointless, but maybe later.
        else -> RenderNodeApi29()
    }

    private fun testHiddenApi() = testRenderNode() && checkCanvasAccess()

    private fun testRenderNode() = try {
        with(newInstance()) {
            setAlpha(getAlpha())
            setCameraDistance(getCameraDistance())
            setElevation(getElevation())
            setPivotX(getPivotX())
            setPivotY(getPivotY())
            setRotationX(getRotationX())
            setRotationY(getRotationY())
            setRotationZ(getRotationZ())
            setScaleX(getScaleX())
            setScaleY(getScaleY())
            setTranslationX(getTranslationX())
            setTranslationY(getTranslationY())
            setTranslationZ(getTranslationZ())

            setOutline(null)
            setPosition(0, 0, 0, 0)
            hasIdentityMatrix()
            getMatrix(Matrix())

            setProjectBackwards(false)
            setProjectionReceiver(false)
            endRecording(beginRecording(0, 0))

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ShadowColorsHelper.testColors(this)
            }
        }
        true
    } catch (e: Throwable) {
        false
    }

    private fun checkCanvasAccess() =
        Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.O_MR1 ||
                IsCanvasReflectorValid
}

internal val IsCanvasReflectorValid: Boolean by lazy {
    try {
        CanvasReflector
        true
    } catch (e: Throwable) {
        false
    }
}

@SuppressLint("SoonBlockedPrivateApi")
internal object CanvasReflector {
    private val requiresDoubleReflection = Build.VERSION.SDK_INT == Build.VERSION_CODES.P

    private val getDeclaredMethod: Method =
        Class::class.java.getDeclaredMethod(
            "getDeclaredMethod",
            String::class.java,
            arrayOf<Class<*>>()::class.java
        )

    private val insertReorderBarrierMethod: Method =
        if (requiresDoubleReflection) {
            getDeclaredMethod.invoke(
                Canvas::class.java,
                "insertReorderBarrier",
                emptyArray<Class<*>>()
            ) as Method
        } else {
            Canvas::class.java.getDeclaredMethod("insertReorderBarrier")
        }

    private val insertInorderBarrierMethod: Method =
        if (requiresDoubleReflection) {
            getDeclaredMethod.invoke(
                Canvas::class.java,
                "insertInorderBarrier",
                emptyArray<Class<*>>()
            ) as Method
        } else {
            Canvas::class.java.getDeclaredMethod("insertInorderBarrier")
        }

    fun insertReorderBarrier(canvas: Canvas) {
        insertReorderBarrierMethod.invoke(canvas)
    }

    fun insertInorderBarrier(canvas: Canvas) {
        insertInorderBarrierMethod.invoke(canvas)
    }
}