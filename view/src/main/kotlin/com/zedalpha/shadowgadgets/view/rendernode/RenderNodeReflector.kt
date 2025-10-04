package com.zedalpha.shadowgadgets.view.rendernode

import android.graphics.Canvas
import android.os.Build
import android.view.View
import com.zedalpha.shadowgadgets.view.internal.requireDeclaredMethod
import java.lang.reflect.Method

internal object RenderNodeReflector {

    private lateinit var create: Method
    private lateinit var setClipToBounds: Method
    private lateinit var setProjectBackwards: Method
    private lateinit var setPosition: Method
    private lateinit var start: Method
    private lateinit var end: Method
    private lateinit var drawRenderNode: Method

    // We can live without this one, though it'd be weird to fail only here.
    private var discardDisplayList: Method? = null

    val isValid: Boolean =
        try {
            val renderNodeClass =
                Class.forName("android.view.RenderNode")
            val canvasClass =
                if (Build.VERSION.SDK_INT in 21..22) {
                    Class.forName("android.view.HardwareCanvas")
                } else {
                    Class.forName("android.view.DisplayListCanvas")
                }
            create =
                requireDeclaredMethod(
                    clazz = renderNodeClass,
                    name = "create",
                    String::class.java,
                    View::class.java
                )
            setClipToBounds =
                requireDeclaredMethod(
                    clazz = renderNodeClass,
                    name = "setClipToBounds",
                    Boolean::class.java
                )
            setProjectBackwards =
                requireDeclaredMethod(
                    clazz = renderNodeClass,
                    name = "setProjectBackwards",
                    Boolean::class.java
                )
            setPosition =
                requireDeclaredMethod(
                    clazz = renderNodeClass,
                    name = "setLeftTopRightBottom",
                    Int::class.java,
                    Int::class.java,
                    Int::class.java,
                    Int::class.java
                )
            start =
                requireDeclaredMethod(
                    clazz = renderNodeClass,
                    name = "start",
                    Int::class.java,
                    Int::class.java
                )
            end =
                requireDeclaredMethod(
                    clazz = renderNodeClass,
                    name = "end",
                    canvasClass
                )
            drawRenderNode =
                requireDeclaredMethod(
                    clazz = canvasClass,
                    name = "drawRenderNode",
                    renderNodeClass
                )
            discardDisplayList =
                requireDeclaredMethod(
                    clazz = renderNodeClass,
                    name = when (Build.VERSION.SDK_INT) {
                        in 21..23 -> "destroyDisplayListData"
                        else -> "discardDisplayList"
                    }
                )

            val renderNode = create.invoke(null, "ProjectorTest", null)
            setClipToBounds.invoke(renderNode, false)
            setProjectBackwards.invoke(renderNode, true)
            setPosition.invoke(renderNode, 0, 0, 1, 1)
            val canvas = start.invoke(renderNode, 1, 1) as Canvas
            end.invoke(renderNode, canvas)
            try {
                discardDisplayList?.invoke(renderNode)
            } catch (_: Throwable) {
                discardDisplayList = null
            }
            true
        } catch (_: Throwable) {
            false
        }

    fun createRenderNode(name: String): Any {
        val node = create.invoke(null, name, null)!!
        setClipToBounds(node, false)
        return node
    }

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

    fun record(renderNode: Any, block: (Canvas) -> Unit) {
        val canvas = start.invoke(renderNode, 0, 0) as Canvas
        try {
            block(canvas)
        } finally {
            end.invoke(renderNode, canvas)
        }
    }

    fun drawRenderNode(canvas: Canvas, renderNode: Any) {
        drawRenderNode.invoke(canvas, renderNode)
    }

    fun discardDisplayList(renderNode: Any) {
        discardDisplayList?.invoke(renderNode)
    }
}