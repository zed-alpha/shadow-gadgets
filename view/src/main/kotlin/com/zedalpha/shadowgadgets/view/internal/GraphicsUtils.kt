package com.zedalpha.shadowgadgets.view.internal

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

internal fun getOutlineRect(outline: Outline, outRect: Rect): Boolean =
    if (Build.VERSION.SDK_INT >= 24) {
        OutlineRectHelper.getRect(outline, outRect)
    } else {
        OutlineRectReflector.getRect(outline, outRect)
    }

internal fun getOutlineRadius(outline: Outline): Float =
    if (Build.VERSION.SDK_INT >= 24) {
        OutlineRectHelper.getRadius(outline)
    } else {
        OutlineRectReflector.getRadius(outline)
    }

internal fun clipOutPath(canvas: Canvas, path: Path) {
    if (Build.VERSION.SDK_INT >= 26) {
        CanvasClipHelper.clipOutPath(canvas, path)
    } else {
        @Suppress("DEPRECATION")
        canvas.clipPath(path, Region.Op.DIFFERENCE)
    }
}

internal fun enableZ(canvas: Canvas) {
    if (Build.VERSION.SDK_INT >= 29) {
        CanvasZHelper.enableZ(canvas)
    } else {
        CanvasZReflector.enableZ(canvas)
    }
}

internal fun disableZ(canvas: Canvas) {
    if (Build.VERSION.SDK_INT >= 29) {
        CanvasZHelper.disableZ(canvas)
    } else {
        CanvasZReflector.disableZ(canvas)
    }
}

@RequiresApi(24)
private object OutlineRectHelper {

    @DoNotInline
    fun getRect(outline: Outline, rect: Rect): Boolean = outline.getRect(rect)

    @DoNotInline
    fun getRadius(outline: Outline): Float = outline.radius
}

private object OutlineRectReflector {

    @SuppressLint("DiscouragedPrivateApi", "PrivateApi")
    private val mRect: Field? =
        try {
            Outline::class.java.getDeclaredField("mRect")
        } catch (_: Exception) {
            null
        }

    fun getRect(outline: Outline, outRect: Rect): Boolean {
        val rectField = mRect ?: return false

        return try {
            val rect = rectField.get(outline) as Rect
            outRect.set(rect)
            true
        } catch (_: Exception) {
            false
        }
    }

    private val mRadius: Field? =
        try {
            @SuppressLint("PrivateApi")
            Outline::class.java.getDeclaredField("mRadius")
        } catch (_: Exception) {
            null
        }

    fun getRadius(outline: Outline): Float {
        val radiusField = mRadius ?: return 0F

        return try {
            radiusField.getFloat(outline)
        } catch (_: Exception) {
            0F
        }
    }
}

@RequiresApi(26)
private object CanvasClipHelper {

    @DoNotInline
    fun clipOutPath(canvas: Canvas, path: Path) {
        canvas.clipOutPath(path)
    }
}

@RequiresApi(29)
private object CanvasZHelper {

    @DoNotInline
    fun enableZ(canvas: Canvas) = canvas.enableZ()

    @DoNotInline
    fun disableZ(canvas: Canvas) = canvas.disableZ()
}

private object CanvasZReflector {

    private val reorderBarrierMethod: Method? =
        getDeclaredMethod(Canvas::class.java, "insertReorderBarrier")

    private val inorderBarrierMethod: Method? =
        getDeclaredMethod(Canvas::class.java, "insertInorderBarrier")

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