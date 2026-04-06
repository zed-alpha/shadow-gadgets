package com.zedalpha.shadowgadgets.view.internal

import android.graphics.Path
import android.graphics.RectF
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearancePathProvider

private class MaterialShapeDrawablePathShaper {

    private val pathProvider = ShapeAppearancePathProvider()

    private val tmpRectF = RectF()

    fun shapePath(msd: MaterialShapeDrawable, path: Path) {
        val bounds = tmpRectF
        bounds.set(msd.bounds)

        pathProvider.calculatePath(
            /* shapeAppearanceModel = */ msd.shapeAppearanceModel,
            /* interpolation = */ msd.interpolation,
            /* bounds = */ bounds,
            /* path = */ path
        )
    }
}

internal fun Path.setFromMaterialShapeDrawable(msd: MaterialShapeDrawable) {
    val pathShaper =
        MaterialShapeDrawablePathShaperThreadLocal.get()
            ?: MaterialShapeDrawablePathShaper()
                .also { MaterialShapeDrawablePathShaperThreadLocal.set(it) }

    pathShaper.shapePath(msd, this)
}

private val MaterialShapeDrawablePathShaperThreadLocal =
    ThreadLocal<MaterialShapeDrawablePathShaper>()