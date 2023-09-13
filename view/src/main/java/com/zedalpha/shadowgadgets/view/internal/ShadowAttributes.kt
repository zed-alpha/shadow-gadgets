package com.zedalpha.shadowgadgets.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.ShadowPlane
import com.zedalpha.shadowgadgets.view.ShadowPlane.Foreground


internal data class ShadowAttributes(
    val id: Int,
    val shadowPlane: ShadowPlane?,
    val clipOutlineShadow: Boolean?,
    val outlineShadowColorCompat: Int?,
    val forceOutlineShadowColorCompat: Boolean?
)

internal fun AttributeSet?.extractShadowAttributes(
    context: Context
): ShadowAttributes {
    val array = context.obtainStyledAttributes(
        this,
        R.styleable.ShadowAttributes
    )
    return ShadowAttributes(
        array.getResourceId(
            R.styleable.ShadowAttributes_android_id,
            View.NO_ID
        ),
        when {
            array.hasValue(R.styleable.ShadowAttributes_shadowPlane) -> {
                R.styleable.ShadowAttributes_shadowPlane
            }

            array.hasValue(R.styleable.ShadowAttributes_clippedShadowPlane) -> {
                R.styleable.ShadowAttributes_clippedShadowPlane
            }

            else -> null
        }?.let { ShadowPlane.forValue(array.getInt(it, Foreground.ordinal)) },
        if (array.hasValue(R.styleable.ShadowAttributes_clipOutlineShadow)) {
            array.getBoolean(
                R.styleable.ShadowAttributes_clipOutlineShadow,
                false
            )
        } else null,
        if (array.hasValue(R.styleable.ShadowAttributes_outlineShadowColorCompat)) {
            array.getColor(
                R.styleable.ShadowAttributes_outlineShadowColorCompat,
                DefaultShadowColorInt
            )
        } else null,
        if (array.hasValue(R.styleable.ShadowAttributes_forceOutlineShadowColorCompat)) {
            array.getBoolean(
                R.styleable.ShadowAttributes_forceOutlineShadowColorCompat,
                false
            )
        } else null
    ).also { array.recycle() }
}