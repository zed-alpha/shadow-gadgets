package com.zedalpha.shadowgadgets.view.internal

import android.content.Context
import android.util.AttributeSet
import android.view.View
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

internal fun AttributeSet?.extractShadowAttributes(context: Context): ShadowAttributes {
    val array =
        context.obtainStyledAttributes(this, R.styleable.ShadowAttributes)
    return ShadowAttributes(
        array.getResourceId(
            /* index = */ R.styleable.ShadowAttributes_android_id,
            /* defValue = */ View.NO_ID
        ),
        if (array.hasValue(R.styleable.ShadowAttributes_shadowPlane)) {
            val value =
                array.getInt(
                    /* index = */ R.styleable.ShadowAttributes_shadowPlane,
                    /* defValue = */ Foreground.ordinal
                )
            ShadowPlane.entries[value]
        } else {
            null
        },
        if (array.hasValue(R.styleable.ShadowAttributes_clipOutlineShadow)) {
            array.getBoolean(
                /* index = */ R.styleable.ShadowAttributes_clipOutlineShadow,
                /* defValue = */ false
            )
        } else {
            null
        },
        if (array.hasValue(R.styleable.ShadowAttributes_outlineShadowColorCompat)) {
            array.getColor(
                /* index = */ R.styleable.ShadowAttributes_outlineShadowColorCompat,
                /* defValue = */ DefaultShadowColor
            )
        } else {
            null
        },
        if (array.hasValue(R.styleable.ShadowAttributes_forceOutlineShadowColorCompat)) {
            array.getBoolean(
                /* index = */ R.styleable.ShadowAttributes_forceOutlineShadowColorCompat,
                /* defValue = */ false
            )
        } else {
            null
        }
    )
        .also { array.recycle() }
}