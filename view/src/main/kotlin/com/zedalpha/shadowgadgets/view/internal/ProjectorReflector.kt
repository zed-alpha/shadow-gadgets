package com.zedalpha.shadowgadgets.view.internal

import android.graphics.Canvas
import android.os.Build
import android.view.View
import java.lang.reflect.Method

internal object ProjectorReflector {

    private lateinit var create: Method

    private lateinit var setClipToBounds: Method

    private lateinit var setProjectBackwards: Method

    private lateinit var setPosition: Method

    private lateinit var start: Method

    private lateinit var end: Method

    private lateinit var drawRenderNode: Method

    // We can live without this one, though it'd be weird to fail only here.
    private var discardDisplayList: Method? = null

    val isValid = try {
        val renderNodeClass = Class.forName("android.view.RenderNode")
        val canvasClass = if (Build.VERSION.SDK_INT in 21..22) {
            Class.forName("android.view.HardwareCanvas")
        } else {
            Class.forName("android.view.DisplayListCanvas")
        }
        if (Build.VERSION.SDK_INT == 28) {
            val getDeclared = Class::class.java.getDeclaredMethod(
                "getDeclaredMethod",
                String::class.java,
                arrayOf<Class<*>>()::class.java
            )
            create = getDeclared.invoke(
                renderNodeClass,
                "create",
                arrayOf(String::class.java, View::class.java)
            ) as Method
            setClipToBounds = getDeclared.invoke(
                renderNodeClass,
                "setClipToBounds",
                arrayOf(Boolean::class.java)
            ) as Method
            setProjectBackwards = getDeclared.invoke(
                renderNodeClass,
                "setProjectBackwards",
                arrayOf(Boolean::class.java)
            ) as Method
            setPosition = getDeclared.invoke(
                renderNodeClass,
                "setLeftTopRightBottom",
                arrayOf(
                    Int::class.java,
                    Int::class.java,
                    Int::class.java,
                    Int::class.java
                )
            ) as Method
            start = getDeclared.invoke(
                renderNodeClass,
                "start",
                arrayOf(Int::class.java, Int::class.java)
            ) as Method
            end = getDeclared.invoke(
                renderNodeClass,
                "end",
                arrayOf(canvasClass)
            ) as Method
            drawRenderNode = getDeclared.invoke(
                canvasClass,
                "drawRenderNode",
                arrayOf(renderNodeClass)
            ) as Method
            discardDisplayList = getDeclared.invoke(
                renderNodeClass,
                "discardDisplayList",
                emptyArray<Class<*>>()
            ) as Method
        } else {
            create = renderNodeClass.getDeclaredMethod(
                "create",
                String::class.java,
                View::class.java
            )
            setClipToBounds = renderNodeClass.getDeclaredMethod(
                "setClipToBounds",
                Boolean::class.java
            )
            setProjectBackwards = renderNodeClass.getDeclaredMethod(
                "setProjectBackwards",
                Boolean::class.java
            )
            setPosition = renderNodeClass.getDeclaredMethod(
                "setLeftTopRightBottom",
                Int::class.java,
                Int::class.java,
                Int::class.java,
                Int::class.java
            )
            start = renderNodeClass.getDeclaredMethod(
                "start",
                Int::class.java,
                Int::class.java
            )
            end = renderNodeClass.getDeclaredMethod(
                "end",
                canvasClass
            )
            drawRenderNode = canvasClass.getDeclaredMethod(
                "drawRenderNode",
                renderNodeClass
            )
            discardDisplayList = renderNodeClass.getDeclaredMethod(
                if (Build.VERSION.SDK_INT in 21..23) {
                    "destroyDisplayListData"
                } else {
                    "discardDisplayList"
                }
            )
        }

        val renderNode = create.invoke(null, "ProjectorTest", null)
        setClipToBounds.invoke(renderNode, false)
        setProjectBackwards.invoke(renderNode, true)
        setPosition.invoke(renderNode, 0, 0, 0, 0)
        val canvas = start.invoke(renderNode, 0, 0)
        end.invoke(renderNode, canvas)
        try {
            discardDisplayList?.invoke(renderNode)
        } catch (e: Throwable) {
            discardDisplayList = null
        }
        true
    } catch (e: Throwable) {
        false
    }

    fun createRenderNode(): Any = create.invoke(null, "Projector", null)!!

    fun setClipToBounds(renderNode: Any, clipToBounds: Boolean) {
        setClipToBounds.invoke(renderNode, clipToBounds)
    }

    fun setProjectBackwards(renderNode: Any, shouldProject: Boolean) {
        setProjectBackwards.invoke(renderNode, shouldProject)
    }

    fun setPosition(
        renderNode: Any,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        setPosition.invoke(renderNode, left, top, right, bottom)
    }

    fun start(renderNode: Any, width: Int, height: Int): Canvas =
        start.invoke(renderNode, width, height) as Canvas

    fun end(renderNode: Any, canvas: Canvas) {
        end.invoke(renderNode, canvas)
    }

    fun drawRenderNode(canvas: Canvas, renderNode: Any) {
        drawRenderNode.invoke(canvas, renderNode)
    }

    fun discardDisplayList(renderNode: Any) {
        discardDisplayList?.invoke(renderNode)
    }
}