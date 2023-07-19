package com.zedalpha.shadowgadgets.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.R


internal data class ClippedShadowAttributes(
    val id: Int,
    val clipOutlineShadow: Boolean? = null,
    val clippedShadowPlane: ClippedShadowPlane? = null
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
        if (array.hasValue(R.styleable.ClippedShadowAttributes_clipOutlineShadow)) {
            array.getBoolean(
                R.styleable.ClippedShadowAttributes_clipOutlineShadow,
                false
            )
        } else null,
        if (array.hasValue(R.styleable.ClippedShadowAttributes_clippedShadowPlane)) {
            ClippedShadowPlane.forValue(
                array.getInt(
                    R.styleable.ClippedShadowAttributes_clippedShadowPlane,
                    0
                )
            )
        } else null
    ).also { array.recycle() }
}