@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.annotation.SuppressLint
import android.graphics.Outline
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.annotation.RequiresApi
import java.lang.reflect.Field
import java.lang.reflect.Method


private val IsRectReflectorValid: Boolean by lazy {
    try {
        RectReflector
        true
    } catch (e: Throwable) {
        false
    }
}

internal val getOutlineRadius: (Outline) -> Float =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> { outline -> outline.radius }
        IsRectReflectorValid -> { outline -> RectReflector.getRadius(outline) }
        else -> { _ -> 0F }
    }

internal val getOutlineRect: (Outline, Rect) -> Boolean =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.N -> { outline, rect -> outline.getRect(rect) }
        IsRectReflectorValid -> { outline, rect -> RectReflector.getRect(outline, rect) }
        else -> { _, _ -> false }
    }

@SuppressLint("DiscouragedPrivateApi", "SoonBlockedPrivateApi")
private object RectReflector {
    private val mRectField: Field = Outline::class.java.getDeclaredField("mRect")
    private val mRadiusField: Field = Outline::class.java.getDeclaredField("mRadius")

    fun getRect(outline: Outline, outRect: Rect): Boolean {
        val rect = mRectField.get(outline) as Rect?
        return rect?.let {
            outRect.set(rect)
            true
        } ?: false
    }

    fun getRadius(outline: Outline) = mRadiusField.getFloat(outline)
}

private val IsPathReflectorValid: Boolean by lazy {
    try {
        PathReflector
        true
    } catch (e: Throwable) {
        false
    }
}

internal fun setOutlinePath(outline: Outline, path: Path) {
    if (IsPathReflectorValid) PathReflector.setPath(outline, path)
}

@SuppressLint("DiscouragedPrivateApi")
private object PathReflector {
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    private val requiresDoubleReflection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P

    private val getDeclaredField: Method =
        Class::class.java.getDeclaredMethod(
            "getDeclaredField",
            String::class.java
        )

    private val mPathField: Field =
        if (requiresDoubleReflection) {
            getDeclaredField.invoke(
                Outline::class.java,
                "mPath"
            ) as Field
        } else {
            Outline::class.java.getDeclaredField("mPath")
        }

    fun setPath(outline: Outline, path: Path) {
        val outlinePath = mPathField.get(outline) as? Path ?: return
        path.set(outlinePath)
    }
}