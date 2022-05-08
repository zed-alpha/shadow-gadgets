@file:RequiresApi(Build.VERSION_CODES.P)

package com.zedalpha.shadowgadgets.drawable

import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Outline
import android.os.Build
import android.view.DisplayListCanvas
import android.view.RenderNode
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.createMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.endMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getAlphaMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getAmbientShadowColorMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getCameraDistanceMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getElevationMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getMatrixMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getPivotXMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getPivotYMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getRotationMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getRotationXMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getRotationYMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getScaleXMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getScaleYMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getSpotShadowColorMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getTranslationXMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getTranslationYMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.getTranslationZMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.hasIdentityMatrixMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setAlphaMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setAmbientShadowColorMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setCameraDistanceMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setElevationMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setLeftTopRightBottomMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setOutlineMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setPivotXMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setPivotYMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setRotationMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setRotationXMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setRotationYMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setScaleXMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setScaleYMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setSpotShadowColorMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setTranslationXMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setTranslationYMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.setTranslationZMethod
import com.zedalpha.shadowgadgets.drawable.RenderNodeReflectorPie.startMethod
import com.zedalpha.shadowgadgets.rendernode.IsCanvasReflectorValid
import com.zedalpha.shadowgadgets.rendernode.RenderNodeColors
import com.zedalpha.shadowgadgets.rendernode.RenderNodeWrapper
import java.lang.reflect.Method


internal val IsPieReflectionAvailable: Boolean by lazy {
    try {
        RenderNodeReflectorPie
        IsCanvasReflectorValid
    } catch (e: Throwable) {
        false
    }
}

internal class PieReflectorWrapper : RenderNodeWrapper, RenderNodeColors {
    private val renderNode: RenderNode =
        createMethod.invoke(null, "OverlayShadow", null) as RenderNode

    private fun recordEmptyDisplayList() {
        val canvas = startMethod.invoke(renderNode, 0, 0) as DisplayListCanvas
        endMethod.invoke(renderNode, canvas)
    }

    override fun getAlpha(): Float =
        getAlphaMethod.invoke(renderNode) as Float

    override fun setAlpha(alpha: Float) =
        setAlphaMethod.invoke(renderNode, alpha) as Boolean

    override fun getCameraDistance(): Float =
        getCameraDistanceMethod.invoke(renderNode) as Float

    override fun setCameraDistance(distance: Float) =
        setCameraDistanceMethod.invoke(renderNode, distance) as Boolean

    override fun getElevation(): Float =
        getElevationMethod.invoke(renderNode) as Float

    override fun setElevation(elevation: Float) =
        setElevationMethod.invoke(renderNode, elevation) as Boolean

    override fun getPivotX(): Float =
        getPivotXMethod.invoke(renderNode) as Float

    override fun setPivotX(pivotX: Float) =
        setPivotXMethod.invoke(renderNode, pivotX) as Boolean

    override fun getPivotY(): Float =
        getPivotYMethod.invoke(renderNode) as Float

    override fun setPivotY(pivotY: Float) =
        setPivotYMethod.invoke(renderNode, pivotY) as Boolean

    override fun getRotationX(): Float =
        getRotationXMethod.invoke(renderNode) as Float

    override fun setRotationX(rotationX: Float) =
        setRotationXMethod.invoke(renderNode, rotationX) as Boolean

    override fun getRotationY(): Float =
        getRotationYMethod.invoke(renderNode) as Float

    override fun setRotationY(rotationY: Float) =
        setRotationYMethod.invoke(renderNode, rotationY) as Boolean

    override fun getRotationZ(): Float =
        getRotationMethod.invoke(renderNode) as Float

    override fun setRotationZ(rotationZ: Float) =
        setRotationMethod.invoke(renderNode, rotationZ) as Boolean

    override fun getScaleX(): Float =
        getScaleXMethod.invoke(renderNode) as Float

    override fun setScaleX(scaleX: Float) =
        setScaleXMethod.invoke(renderNode, scaleX) as Boolean

    override fun getScaleY(): Float =
        getScaleYMethod.invoke(renderNode) as Float

    override fun setScaleY(scaleY: Float) =
        setScaleYMethod.invoke(renderNode, scaleY) as Boolean

    override fun getTranslationX(): Float =
        getTranslationXMethod.invoke(renderNode) as Float

    override fun setTranslationX(translationX: Float) =
        setTranslationXMethod.invoke(renderNode, translationX) as Boolean

    override fun getTranslationY(): Float =
        getTranslationYMethod.invoke(renderNode) as Float

    override fun setTranslationY(translationY: Float) =
        setTranslationYMethod.invoke(renderNode, translationY) as Boolean

    override fun getTranslationZ(): Float =
        getTranslationZMethod.invoke(renderNode) as Float

    override fun setTranslationZ(translationZ: Float) =
        setTranslationZMethod.invoke(renderNode, translationZ) as Boolean

    override fun getAmbientShadowColor(): Int =
        getAmbientShadowColorMethod.invoke(renderNode) as Int

    override fun setAmbientShadowColor(color: Int) =
        setAmbientShadowColorMethod.invoke(renderNode, color) as Boolean

    override fun getSpotShadowColor(): Int =
        getSpotShadowColorMethod.invoke(renderNode) as Int

    override fun setSpotShadowColor(color: Int) =
        setSpotShadowColorMethod.invoke(renderNode, color) as Boolean

    override fun setOutline(outline: Outline?) =
        setOutlineMethod.invoke(renderNode, outline) as Boolean

    override fun setPosition(left: Int, top: Int, right: Int, bottom: Int) =
        setLeftTopRightBottomMethod(renderNode, left, top, right, bottom) as Boolean

    override fun hasIdentityMatrix() =
        hasIdentityMatrixMethod.invoke(renderNode) as Boolean

    override fun getMatrix(outMatrix: Matrix) {
        getMatrixMethod.invoke(renderNode, outMatrix)
    }

    override fun setProjectBackwards(shouldProject: Boolean): Boolean {
        throw UnsupportedOperationException()
    }

    override fun setProjectionReceiver(shouldReceive: Boolean): Boolean {
        throw UnsupportedOperationException()
    }

    override fun beginRecording(width: Int, height: Int): Canvas {
        throw UnsupportedOperationException()
    }

    override fun endRecording(canvas: Canvas) {
        throw UnsupportedOperationException()
    }

    override fun draw(canvas: Canvas) {
        if (!renderNode.isValid) recordEmptyDisplayList()
        (canvas as DisplayListCanvas).drawRenderNode(renderNode)
    }
}

private object RenderNodeReflectorPie {
    val getDeclaredMethod: Method = Class::class.java.getDeclaredMethod(
        "getDeclaredMethod",
        String::class.java,
        arrayOf<Class<*>>()::class.java
    )

    val createMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "create",
        arrayOf(String::class.java, View::class.java)
    ) as Method

    val startMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "start",
        arrayOf(Int::class.java, Int::class.java)
    ) as Method

    val endMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "end",
        arrayOf(DisplayListCanvas::class.java)
    ) as Method

    val getAlphaMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getAlpha",
        emptyArray<Class<*>>()
    ) as Method

    val setAlphaMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setAlpha",
        arrayOf(Float::class.java)
    ) as Method

    val getCameraDistanceMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getCameraDistance",
        emptyArray<Class<*>>()
    ) as Method

    val setCameraDistanceMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setCameraDistance",
        arrayOf(Float::class.java)
    ) as Method

    val getElevationMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getElevation",
        emptyArray<Class<*>>()
    ) as Method

    val setElevationMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setElevation",
        arrayOf(Float::class.java)
    ) as Method

    val getPivotXMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getPivotX",
        emptyArray<Class<*>>()
    ) as Method

    val setPivotXMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setPivotX",
        arrayOf(Float::class.java)
    ) as Method

    val getPivotYMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getPivotY",
        emptyArray<Class<*>>()
    ) as Method

    val setPivotYMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setPivotY",
        arrayOf(Float::class.java)
    ) as Method

    val getRotationXMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getRotationX",
        emptyArray<Class<*>>()
    ) as Method

    val setRotationXMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setRotationX",
        arrayOf(Float::class.java)
    ) as Method

    val getRotationYMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getRotationY",
        emptyArray<Class<*>>()
    ) as Method

    val setRotationYMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setRotationY",
        arrayOf(Float::class.java)
    ) as Method

    val getRotationMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getRotation",
        emptyArray<Class<*>>()
    ) as Method

    val setRotationMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setRotation",
        arrayOf(Float::class.java)
    ) as Method

    val getScaleXMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getScaleX",
        emptyArray<Class<*>>()
    ) as Method

    val setScaleXMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setScaleX",
        arrayOf(Float::class.java)
    ) as Method

    val getScaleYMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getScaleY",
        emptyArray<Class<*>>()
    ) as Method

    val setScaleYMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setScaleY",
        arrayOf(Float::class.java)
    ) as Method

    val getTranslationXMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getTranslationX",
        emptyArray<Class<*>>()
    ) as Method

    val setTranslationXMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setTranslationX",
        arrayOf(Float::class.java)
    ) as Method

    val getTranslationYMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getTranslationY",
        emptyArray<Class<*>>()
    ) as Method

    val setTranslationYMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setTranslationY",
        arrayOf(Float::class.java)
    ) as Method

    val getTranslationZMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getTranslationZ",
        emptyArray<Class<*>>()
    ) as Method

    val setTranslationZMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setTranslationZ",
        arrayOf(Float::class.java)
    ) as Method

    val getAmbientShadowColorMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getAmbientShadowColor",
        emptyArray<Class<*>>()
    ) as Method

    val setAmbientShadowColorMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setAmbientShadowColor",
        arrayOf(Int::class.java)
    ) as Method

    val getSpotShadowColorMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getSpotShadowColor",
        emptyArray<Class<*>>()
    ) as Method

    val setSpotShadowColorMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setSpotShadowColor",
        arrayOf(Int::class.java)
    ) as Method

    val setOutlineMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setOutline",
        arrayOf(Outline::class.java)
    ) as Method

    val setLeftTopRightBottomMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "setLeftTopRightBottom",
        arrayOf(Int::class.java, Int::class.java, Int::class.java, Int::class.java)
    ) as Method

    val hasIdentityMatrixMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "hasIdentityMatrix",
        emptyArray<Class<*>>()
    ) as Method

    val getMatrixMethod: Method = getDeclaredMethod.invoke(
        RenderNode::class.java,
        "getMatrix",
        arrayOf(Matrix::class.java)
    ) as Method
}