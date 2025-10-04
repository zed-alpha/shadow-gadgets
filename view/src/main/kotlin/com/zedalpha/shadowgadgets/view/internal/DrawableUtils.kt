package com.zedalpha.shadowgadgets.view.internal

import android.annotation.SuppressLint
import android.graphics.Path
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableContainer
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.RotateDrawable
import android.graphics.drawable.ScaleDrawable
import android.os.Build
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearancePathProvider
import java.lang.reflect.Field

internal fun Drawable.findMaterialShapeDrawable(): MaterialShapeDrawable? =
    this.findDrawable()

internal inline fun <reified T : Drawable> Drawable.findDrawable(): T? =
    DrawableFinder(this, T::class.java).find()

internal class DrawableFinder<T>(val root: Drawable, val clazz: Class<T>)
        where T : Drawable {

    fun find(): T? = root.find()

    private fun Drawable.find(): T? =
        with(this) {
            if (clazz.isInstance(this)) return clazz.cast(this)
            if (this is DrawableContainer) return this.current.find()
            if (this is DrawableWrapperCompat) return this.drawable?.find()
            if (Build.VERSION.SDK_INT >= 23) {
                if (this is DrawableWrapper) return this.drawable?.find()
            } else {
                if (this is ClipDrawable) return this.drawableLollipop?.find()
                if (this is InsetDrawable) return this.drawable?.find()
                if (this is RotateDrawable) return this.drawable?.find()
                if (this is ScaleDrawable) return this.drawable?.find()
            }
            if (this is LayerDrawable) {
                return (0..<this.numberOfLayers)
                    .asSequence()
                    .map { this.getDrawable(it)?.find() }
                    .firstNotNullOfOrNull { it }
            }
            return null
        }
}

private val ClipDrawable.drawableLollipop: Drawable?
    get() = ClipDrawableReflector.getDrawable(this)

private object ClipDrawableReflector {

    private val mClipState: Field? =
        try {
            @SuppressLint("PrivateApi")
            ClipDrawable::class.java
                .getDeclaredField("mClipState")
                .apply { isAccessible = true }
        } catch (_: Exception) {
            null
        }

    private val mDrawableField: Field? =
        try {
            @SuppressLint("PrivateApi")
            Class.forName($$"android.graphics.drawable.ClipDrawable$ClipState")
                .getDeclaredField("mDrawable")
                .apply { isAccessible = true }
        } catch (_: Exception) {
            null
        }

    fun getDrawable(clipDrawable: ClipDrawable): Drawable? {
        val state = mClipState?.get(clipDrawable) ?: return null
        return mDrawableField?.get(state) as? Drawable
    }
}

internal fun Path.setFromMaterialShapeDrawable(msd: MaterialShapeDrawable) {
    val pathProvider =
        ShapeAppearancePathProviderThreadLocal.get()
            ?: ShapeAppearancePathProvider()
                .also { ShapeAppearancePathProviderThreadLocal.set(it) }

    val bounds = ThreadLocalGraphicsTemps.rectF
    bounds.set(msd.bounds)

    pathProvider.calculatePath(
        /* shapeAppearanceModel = */ msd.shapeAppearanceModel,
        /* interpolation = */ msd.interpolation,
        /* bounds = */ bounds,
        /* path = */ this
    )
}

private val ShapeAppearancePathProviderThreadLocal =
    ThreadLocal<ShapeAppearancePathProvider>()