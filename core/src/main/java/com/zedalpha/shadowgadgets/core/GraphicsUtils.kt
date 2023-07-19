package com.zedalpha.shadowgadgets.core

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import java.lang.reflect.Field
import java.lang.reflect.Method


internal object OutlinePathReflector {

    private var mPath: Field? = null

    private var mPathInitialized = false

    @SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
    private fun getPathField() = when {
        mPathInitialized -> mPath
        else -> {
            mPathInitialized = true
            try {
                if (Build.VERSION.SDK_INT >= 28) {
                    Class::class.java.getDeclaredMethod(
                        "getDeclaredField",
                        String::class.java
                    ).invoke(Outline::class.java, "mPath") as Field
                } else {
                    Outline::class.java.getDeclaredField("mPath")
                }.also { mPath = it }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getPath(path: Path, outline: Outline): Boolean {
        val pathField = getPathField() ?: return false
        val outlinePath = try {
            pathField.get(outline) as Path
        } catch (e: Exception) {
            null
        }
        return outlinePath?.let { path.set(it); true } ?: false
    }
}

@RequiresApi(24)
internal object OutlineRect24 {

    @DoNotInline
    fun getRect(outline: Outline, rect: Rect) = outline.getRect(rect)

    @DoNotInline
    fun getRadius(outline: Outline) = outline.radius
}

internal object OutlineRectReflector {

    private var mRect: Field? = null

    private var mRectInitialized = false

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun getRectField() = when {
        mRectInitialized -> mRect
        else -> {
            mRectInitialized = true
            try {
                Outline::class.java.getDeclaredField("mRect")
                    .also { mRect = it }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getRect(outline: Outline, outRect: Rect): Boolean {
        val rectField = getRectField() ?: return false
        val rect = try {
            rectField.get(outline) as Rect
        } catch (e: Exception) {
            null
        }
        return rect?.let { outRect.set(rect); true } ?: false
    }

    @SuppressLint("SoonBlockedPrivateApi")
    private var mRadius: Field? = null

    private var mRadiusInitialized = false

    @SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
    private fun getRadiusField() = when {
        mRadiusInitialized -> mRadius
        else -> {
            mRadiusInitialized = true
            try {
                Outline::class.java.getDeclaredField("mRadius")
                    .also { mRadius = it }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getRadius(outline: Outline): Float {
        val radiusField = getRadiusField() ?: return 0F
        return try {
            radiusField.getFloat(outline)
        } catch (e: Exception) {
            0F
        }
    }
}

@RequiresApi(26)
internal object CanvasClip26 {

    @DoNotInline
    fun clipOutPath(canvas: Canvas, path: Path) {
        canvas.clipOutPath(path)
    }
}

@RequiresApi(29)
internal object CanvasZ29 {

    @DoNotInline
    fun enableZ(canvas: Canvas) {
        canvas.enableZ()
    }

    @DoNotInline
    fun disableZ(canvas: Canvas) {
        canvas.disableZ()
    }
}

internal object CanvasZReflector {

    private var getDeclared: Method? = null

    private var getDeclaredInitialized = false

    private fun getDeclaredMethod() = when {
        getDeclaredInitialized -> getDeclared
        else -> {
            getDeclaredInitialized = true
            try {
                Class::class.java.getDeclaredMethod(
                    "getDeclaredMethod",
                    String::class.java,
                    arrayOf<Class<*>>()::class.java
                ).also { getDeclared = it }
            } catch (e: Exception) {
                null
            }
        }
    }

    @SuppressLint("PrivateApi")
    private val reorderBarrierMethod = when (Build.VERSION.SDK_INT) {
        28 -> try {
            getDeclaredMethod()?.invoke(
                Canvas::class.java,
                "insertReorderBarrier",
                emptyArray<Class<*>>()
            ) as Method?
        } catch (e: Exception) {
            null
        }

        else -> try {
            Canvas::class.java.getDeclaredMethod("insertReorderBarrier")
        } catch (e: Exception) {
            null
        }
    }?.apply { isAccessible = true }

    @SuppressLint("PrivateApi")
    private val inorderBarrierMethod = when (Build.VERSION.SDK_INT) {
        28 -> try {
            getDeclaredMethod()?.invoke(
                Canvas::class.java,
                "insertInorderBarrier",
                emptyArray<Class<*>>()
            ) as Method?
        } catch (e: Exception) {
            null
        }

        else -> try {
            Canvas::class.java.getDeclaredMethod("insertInorderBarrier")
        } catch (e: Exception) {
            null
        }
    }?.apply { isAccessible = true }

    fun enableZ(canvas: Canvas) {
        try {
            reorderBarrierMethod?.invoke(canvas)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun disableZ(canvas: Canvas) {
        try {
            inorderBarrierMethod?.invoke(canvas)
        } catch (e: Exception) {
            // ignore
        }
    }
}