@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.viewgroup

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.View
import androidx.annotation.IntDef
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.R
import com.zedalpha.shadowgadgets.viewgroup.ClippedShadowsViewGroup.ShadowPlane


sealed interface ClippedShadowsViewGroup {
    val isUsingShadowsFallback: Boolean

    var clipAllChildShadows: Boolean

    var disableChildShadowsOnFallback: Boolean

    @ShadowPlane
    var childClippedShadowPlane: Int

    fun setChildClipOutlineShadow(
        child: View,
        clipOutlineShadow: Boolean,
        disableShadowOnFallback: Boolean,
        @ShadowPlane clippedShadowPlane: Int
    )

    companion object Plane {
        const val FOREGROUND = 0
        const val BACKGROUND = 1
    }

    @IntDef(FOREGROUND, BACKGROUND)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ShadowPlane
}

internal sealed interface ClippedShadowsLayoutParams {
    var clipOutlineShadow: Boolean

    var disableShadowOnFallback: Boolean

    @ShadowPlane
    var clippedShadowPlane: Int
}

internal val View.clippedShadowsLayoutParams
    get() = layoutParams as? ClippedShadowsLayoutParams

internal fun AttributeSet?.extractClippedShadowsLayoutParamsValues(
    context: Context,
    layoutParams: ClippedShadowsLayoutParams
) {
    val array = context.obtainStyledAttributes(this, R.styleable.ClippedShadowsLayoutParams)
    layoutParams.clipOutlineShadow =
        array.getBoolean(R.styleable.ClippedShadowsLayoutParams_clipOutlineShadow, false)
    layoutParams.disableShadowOnFallback =
        array.getBoolean(R.styleable.ClippedShadowsLayoutParams_disableShadowOnFallback, false)
    layoutParams.clippedShadowPlane = array.getInt(
        R.styleable.ClippedShadowsLayoutParams_clippedShadowPlane,
        ClippedShadowsViewGroup.FOREGROUND
    )
    array.recycle()
}