package com.zedalpha.shadowgadgets.view.internal

import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat
import com.google.android.material.shape.MaterialShapeDrawable
import java.lang.reflect.Method


internal fun findMaterialShapeDrawable(drawable: Drawable): MaterialShapeDrawable? =
    when (drawable) {
        is MaterialShapeDrawable -> drawable
        is DrawableWrapperCompat -> drawable.drawable as? MaterialShapeDrawable
        is InsetDrawable -> drawable.drawable as? MaterialShapeDrawable
        is LayerDrawable -> {
            repeat(drawable.numberOfLayers) { index ->
                val layer = drawable.getDrawable(index)
                if (layer is MaterialShapeDrawable) return layer
            }
            null
        }

        else -> when {
            Build.VERSION.SDK_INT >= 23 && drawable is DrawableWrapper -> {
                drawable.drawable as? MaterialShapeDrawable
            }

            else -> null
        }
    }

internal object MaterialShapeDrawableReflector {

    private var getBoundsAsRectF: Method? = null

    private var getBoundsAsRectFInitialized = false

    private fun getBoundsMethod() = when {
        getBoundsAsRectFInitialized -> getBoundsAsRectF
        else -> {
            getBoundsAsRectFInitialized = true
            try {
                MaterialShapeDrawable::class.java
                    .getDeclaredMethod("getBoundsAsRectF")
                    .also { it.isAccessible = true; getBoundsAsRectF = it }
            } catch (e: Exception) {
                null
            }
        }
    }

    private var calculatePath: Method? = null

    private var calculatePathInitialized = false

    private fun getCalculateMethod() = when {
        calculatePathInitialized -> calculatePath
        else -> {
            calculatePathInitialized = true
            try {
                MaterialShapeDrawable::class.java
                    .getDeclaredMethod(
                        "calculatePath",
                        RectF::class.java,
                        Path::class.java
                    )
                    .also { it.isAccessible = true; calculatePath = it }
            } catch (e: Exception) {
                null
            }
        }
    }

    fun setPathFromDrawable(
        path: Path,
        drawable: MaterialShapeDrawable
    ): Boolean {
        val boundsMethod = getBoundsMethod() ?: return false
        return try {
            val rect = boundsMethod.invoke(drawable) as? RectF ?: return false
            val calculateMethod = getCalculateMethod() ?: return false
            calculateMethod.invoke(drawable, rect, path)
            true
        } catch (e: Exception) {
            false
        }
    }
}