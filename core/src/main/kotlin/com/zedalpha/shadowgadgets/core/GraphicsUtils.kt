package com.zedalpha.shadowgadgets.core

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.graphics.Region
import android.os.Build
import androidx.annotation.DoNotInline
import androidx.annotation.RequiresApi
import java.lang.reflect.Field
import java.lang.reflect.Method

public fun getOutlineRect(outline: Outline, outRect: Rect): Boolean =
    if (Build.VERSION.SDK_INT >= 24) {
        OutlineRectHelper.getRect(outline, outRect)
    } else {
        OutlineRectReflector.getRect(outline, outRect)
    }

public fun getOutlineRadius(outline: Outline): Float =
    if (Build.VERSION.SDK_INT >= 24) {
        OutlineRectHelper.getRadius(outline)
    } else {
        OutlineRectReflector.getRadius(outline)
    }

public fun clipOutPath(canvas: Canvas, path: Path) {
    if (Build.VERSION.SDK_INT >= 26) {
        CanvasClipHelper.clipOutPath(canvas, path)
    } else {
        @Suppress("DEPRECATION")
        canvas.clipPath(path, Region.Op.DIFFERENCE)
    }
}

public fun enableZ(canvas: Canvas) {
    if (Build.VERSION.SDK_INT >= 29) {
        CanvasZHelper.enableZ(canvas)
    } else {
        CanvasZReflector.enableZ(canvas)
    }
}

public fun disableZ(canvas: Canvas) {
    if (Build.VERSION.SDK_INT >= 29) {
        CanvasZHelper.disableZ(canvas)
    } else {
        CanvasZReflector.disableZ(canvas)
    }
}

internal object OutlinePathReflector {

    private var mPath: Field? = null

    private var mPathInitialized = false

    @SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
    private fun getPathField(): Field? =
        if (mPathInitialized) {
            mPath
        } else {
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
            } catch (_: Exception) {
                null
            }
        }

    fun getPath(path: Path, outline: Outline): Boolean {
        val pathField = getPathField() ?: return false
        val outlinePath = try {
            pathField.get(outline) as Path
        } catch (_: Exception) {
            null
        }
        @Suppress("NullableBooleanElvis")
        return outlinePath?.let { path.set(it); true } ?: false
    }
}

@RequiresApi(24)
internal object OutlineRectHelper {

    @DoNotInline
    fun getRect(outline: Outline, rect: Rect) = outline.getRect(rect)

    @DoNotInline
    fun getRadius(outline: Outline) = outline.radius
}

internal object OutlineRectReflector {

    private var mRect: Field? = null

    private var mRectInitialized = false

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private fun getRectField(): Field? =
        if (mRectInitialized) {
            mRect
        } else {
            mRectInitialized = true
            try {
                Outline::class.java.getDeclaredField("mRect")
                    .also { mRect = it }
            } catch (_: Exception) {
                null
            }
        }

    fun getRect(outline: Outline, outRect: Rect): Boolean {
        val rectField = getRectField() ?: return false
        val rect = try {
            rectField.get(outline) as Rect
        } catch (_: Exception) {
            null
        }
        @Suppress("NullableBooleanElvis")
        return rect?.let { outRect.set(rect); true } ?: false
    }

    private var mRadius: Field? = null

    private var mRadiusInitialized = false

    @SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
    private fun getRadiusField(): Field? =
        if (mRadiusInitialized) {
            mRadius
        } else {
            mRadiusInitialized = true
            try {
                Outline::class.java.getDeclaredField("mRadius")
                    .also { mRadius = it }
            } catch (_: Exception) {
                null
            }
        }

    fun getRadius(outline: Outline): Float {
        val radiusField = getRadiusField() ?: return 0F
        return try {
            radiusField.getFloat(outline)
        } catch (_: Exception) {
            0F
        }
    }
}

@RequiresApi(26)
internal object CanvasClipHelper {

    @DoNotInline
    fun clipOutPath(canvas: Canvas, path: Path) {
        canvas.clipOutPath(path)
    }
}

@RequiresApi(29)
internal object CanvasZHelper {

    @DoNotInline
    fun enableZ(canvas: Canvas) = canvas.enableZ()

    @DoNotInline
    fun disableZ(canvas: Canvas) = canvas.disableZ()
}

internal object CanvasZReflector {

    private var getDeclared: Method? = null

    private var getDeclaredInitialized = false

    private fun getDeclaredMethod(): Method? =
        if (getDeclaredInitialized) {
            getDeclared
        } else {
            getDeclaredInitialized = true
            try {
                Class::class.java.getDeclaredMethod(
                    "getDeclaredMethod",
                    String::class.java,
                    arrayOf<Class<*>>()::class.java
                ).also { getDeclared = it }
            } catch (_: Exception) {
                null
            }
        }

    @SuppressLint("PrivateApi")
    private val reorderBarrierMethod: Method? = run {
        val method = if (Build.VERSION.SDK_INT == 28) {
            try {
                getDeclaredMethod()?.invoke(
                    Canvas::class.java,
                    "insertReorderBarrier",
                    emptyArray<Class<*>>()
                ) as Method?
            } catch (_: Exception) {
                null
            }
        } else {
            try {
                Canvas::class.java.getDeclaredMethod("insertReorderBarrier")
            } catch (_: Exception) {
                null
            }
        }
        method?.apply { isAccessible = true }
    }

    @SuppressLint("PrivateApi")
    private val inorderBarrierMethod: Method? = run {
        val method = if (Build.VERSION.SDK_INT == 28) {
            try {
                getDeclaredMethod()?.invoke(
                    Canvas::class.java,
                    "insertInorderBarrier",
                    emptyArray<Class<*>>()
                ) as Method?
            } catch (_: Exception) {
                null
            }
        } else {
            try {
                Canvas::class.java.getDeclaredMethod("insertInorderBarrier")
            } catch (_: Exception) {
                null
            }
        }
        method?.apply { isAccessible = true }
    }

    fun enableZ(canvas: Canvas) {
        try {
            reorderBarrierMethod?.invoke(canvas)
        } catch (_: Exception) {
            // ignore
        }
    }

    fun disableZ(canvas: Canvas) {
        try {
            inorderBarrierMethod?.invoke(canvas)
        } catch (_: Exception) {
            // ignore
        }
    }
}