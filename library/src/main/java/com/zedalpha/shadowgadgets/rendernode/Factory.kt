@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.rendernode

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Outline
import android.os.Build
import androidx.annotation.RequiresApi
import java.lang.reflect.Method


internal interface RenderNodeWrapper {
    fun setElevation(elevation: Float)
    fun setTranslationZ(translationZ: Float)
    fun setOutline(outline: Outline?)
    fun setPosition(left: Int, top: Int, right: Int, bottom: Int): Boolean

    fun draw(canvas: Canvas)
}

internal object RenderNodeFactory {
    val isOpenForBusiness = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q || testHiddenApi()

    val newInstance: () -> RenderNodeWrapper =
        when (Build.VERSION.SDK_INT) {
            Build.VERSION_CODES.LOLLIPOP, Build.VERSION_CODES.LOLLIPOP_MR1 -> ::RenderNodeApi21
            in Build.VERSION_CODES.M..Build.VERSION_CODES.P -> ::RenderNodeApi23
            else -> ::RenderNodeApi29
        }

    private fun testHiddenApi() = testRenderNode() && checkCanvasAccess()

    private fun testRenderNode() = try {
        val test = newInstance()
        test.setElevation(0F)
        test.setTranslationZ(0F)
        test.setOutline(null)
        test.setPosition(0, 0, 0, 0)
        true
    } catch (e: Exception) {
        false
    }

    private fun checkCanvasAccess() =
        Build.VERSION.SDK_INT in Build.VERSION_CODES.M..Build.VERSION_CODES.O_MR1 ||
                CanvasReflector.isValid
}

@SuppressLint("SoonBlockedPrivateApi")
internal object CanvasReflector {
    val isValid: Boolean

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

    init {
        isValid = try {
            insertReorderBarrierMethod
            insertInorderBarrierMethod
            true
        } catch (e: Exception) {
            false
        }
    }

    fun insertReorderBarrier(canvas: Canvas) {
        insertReorderBarrierMethod.invoke(canvas)
    }

    fun insertInorderBarrier(canvas: Canvas) {
        insertInorderBarrierMethod.invoke(canvas)
    }
}