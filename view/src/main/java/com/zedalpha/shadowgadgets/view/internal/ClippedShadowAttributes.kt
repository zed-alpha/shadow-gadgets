package com.zedalpha.shadowgadgets.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.R


internal data class ClippedShadowAttributes(
    val id: Int,
    val clipOutlineShadow: Boolean,
    val clippedShadowPlane: ClippedShadowPlane,
    @ColorInt val outlineShadowColorCompat: Int,
    val forceShadowColorCompat: Boolean
)

internal fun AttributeSet?.extractShadowAttributes(
    context: Context
): ClippedShadowAttributes {
    val array = context.obtainStyledAttributes(
        this,
        R.styleable.ClippedShadowAttributes
    )
    return ClippedShadowAttributes(
        array.getResourceId(
            R.styleable.ClippedShadowAttributes_android_id,
            View.NO_ID
        ),
        array.getBoolean(
            R.styleable.ClippedShadowAttributes_clipOutlineShadow,
            false
        ),
        ClippedShadowPlane.forValue(
            array.getInt(
                R.styleable.ClippedShadowAttributes_clippedShadowPlane,
                ClippedShadowPlane.Foreground.ordinal
            )
        ),
        array.getColor(
            R.styleable.ClippedShadowAttributes_outlineShadowColorCompat,
            DefaultShadowColorInt
        ),
        array.getBoolean(
            R.styleable.ClippedShadowAttributes_forceShadowColorCompat,
            false
        )
    ).also { array.recycle() }
}