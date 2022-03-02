@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.rendernode

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import java.lang.reflect.Method


internal interface RenderNodeWrapper {
    fun initialize() {}

    fun setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float): Boolean
    fun setCameraDistance(distance: Float): Boolean
    fun setElevation(elevation: Float): Boolean
    fun setOutline(outline: Outline?): Boolean
    fun setPivotX(pivotX: Float): Boolean
    fun setPivotY(pivotY: Float): Boolean
    fun setPosition(left: Int, top: Int, right: Int, bottom: Int): Boolean
    fun setRotationX(rotationX: Float): Boolean
    fun setRotationY(rotationY: Float): Boolean
    fun setRotationZ(rotation: Float): Boolean
    fun setScaleX(scaleX: Float): Boolean
    fun setScaleY(scaleY: Float): Boolean
    fun setTranslationX(translationX: Float): Boolean
    fun setTranslationY(translationY: Float): Boolean
    fun setTranslationZ(translationZ: Float): Boolean

    fun hasIdentityMatrix(): Boolean
    fun getMatrix(outMatrix: Matrix)

    fun draw(canvas: Canvas)
}

@RequiresApi(Build.VERSION_CODES.P)
internal interface RenderNodeColors {
    fun setAmbientShadowColor(@ColorInt color: Int): Boolean
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
    }.apply { initialize() }

    private fun testHiddenApi() = testRenderNode() && checkCanvasAccess()

    private fun testRenderNode() = try {
        with(newInstance()) {
            setAlpha(0F)
            setCameraDistance(0F)
            setElevation(0F)
            setOutline(null)
            setPivotX(0F)
            setPivotY(0F)
            setPosition(0, 0, 0, 0)
            setRotationX(0F)
            setRotationY(0F)
            setRotationZ(0F)
            setScaleX(0F)
            setScaleY(0F)
            setTranslationX(0F)
            setTranslationY(0F)
            setTranslationZ(0F)
            hasIdentityMatrix()
            getMatrix(Matrix())
        }
        true
    } catch (e: Throwable) {
        false
    }

    private fun checkCanvasAccess() =
        Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.O_MR1 ||
                CanvasReflector.isValid
}

@SuppressLint("SoonBlockedPrivateApi")
internal object CanvasReflector {
    private val requiresDoubleReflection = Build.VERSION.SDK_INT == Build.VERSION_CODES.P

    private val getDeclaredMethod: Method by lazy {
        Class::class.java.getDeclaredMethod(
            "getDeclaredMethod",
            String::class.java,
            arrayOf<Class<*>>()::class.java
        )
    }

    private val insertReorderBarrierMethod: Method by lazy {
        if (requiresDoubleReflection) {
            getDeclaredMethod.invoke(
                Canvas::class.java,
                "insertReorderBarrier",
                emptyArray<Class<*>>()
            ) as Method
        } else {
            Canvas::class.java.getDeclaredMethod("insertReorderBarrier")
        }
    }

    private val insertInorderBarrierMethod: Method by lazy {
        if (requiresDoubleReflection) {
            getDeclaredMethod.invoke(
                Canvas::class.java,
                "insertInorderBarrier",
                emptyArray<Class<*>>()
            ) as Method
        } else {
            Canvas::class.java.getDeclaredMethod("insertInorderBarrier")
        }
    }

    val isValid = try {
        insertReorderBarrierMethod
        insertInorderBarrierMethod
        true
    } catch (e: Throwable) {
        false
    }

    fun insertReorderBarrier(canvas: Canvas) {
        insertReorderBarrierMethod.invoke(canvas)
    }

    fun insertInorderBarrier(canvas: Canvas) {
        insertInorderBarrierMethod.invoke(canvas)
    }
}