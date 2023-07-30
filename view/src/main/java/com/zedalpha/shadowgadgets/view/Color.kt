package com.zedalpha.shadowgadgets.view

import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.view.shadow.ShadowSwitch
import com.zedalpha.shadowgadgets.view.shadow.shadow


@get:ColorInt
@setparam:ColorInt
var View.outlineShadowColorCompat: Int
    get() = getTag(R.id.outline_shadow_color_compat) as? Int
        ?: DefaultShadowColorInt
    set(value) {
        if (value == outlineShadowColorCompat) return
        setTag(R.id.outline_shadow_color_compat, value)
        updateRequiresColor()
        shadow?.updateFilter(value)
    }

var View.forceShadowColorCompat: Boolean
    get() = getTag(R.id.force_shadow_color_compat) == true
    set(value) {
        if (value == forceShadowColorCompat) return
        setTag(R.id.force_shadow_color_compat, value)
        updateRequiresColor()
        shadow?.invalidate()
    }

private fun View.updateRequiresColor() {
    requiresColor = outlineShadowColorCompat != DefaultShadowColorInt &&
            (Build.VERSION.SDK_INT < 28 || forceShadowColorCompat)
}

internal var View.requiresColor: Boolean
    get() = getTag(R.id.requires_color) == true
    private set(value) {
        if (requiresColor == value) return
        setTag(R.id.requires_color, value)
        ShadowSwitch.notifyPropertyChanged(this)
    }