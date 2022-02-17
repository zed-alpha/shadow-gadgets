@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.rendernode

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import androidx.annotation.RequiresApi
import java.lang.reflect.Method


internal interface RenderNodeWrapper {
    fun initialize() {}

    fun setAlpha(@FloatRange(from = 0.0, to = 1.0) alpha: Float)
    fun setCameraDistance(distance: Float)
    fun setElevation(elevation: Float)
    fun setOutline(outline: Outline?)
    fun setPivotX(pivotX: Float)
    fun setPivotY(pivotY: Float)
    fun setPosition(left: Int, top: Int, right: Int, bottom: Int)
    fun setRotationX(rotationX: Float)
    fun setRotationY(rotationY: Float)
    fun setRotationZ(rotation: Float)
    fun setScaleX(scaleX: Float)
    fun setScaleY(scaleY: Float)
    fun setTranslationX(translationX: Float)
    fun setTranslationY(translationY: Float)
    fun setTranslationZ(translationZ: Float)

    fun setAmbientShadowColor(@ColorInt color: Int) {}
    fun setSpotShadowColor(@ColorInt color: Int) {}

    fun draw(canvas: Canvas)
}

internal object RenderNodeFactory {
    val isOpenForBusiness = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || testHiddenApi()

    @SuppressLint("NewApi")
    fun newInstance() = when (Build.VERSION.SDK_INT) {
        Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1 -> RenderNodeApi21()
        in Build.VERSION_CODES.M..Build.VERSION_CODES.P -> RenderNodeApi23()
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
        }
        true
    } catch (e: Error) {
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
    } catch (e: Error) {
        false
    }

    fun insertReorderBarrier(canvas: Canvas) {
        insertReorderBarrierMethod.invoke(canvas)
    }

    fun insertInorderBarrier(canvas: Canvas) {
        insertInorderBarrierMethod.invoke(canvas)
    }
}