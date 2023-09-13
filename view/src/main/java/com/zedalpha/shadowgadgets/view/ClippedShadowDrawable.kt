package com.zedalpha.shadowgadgets.view

import android.graphics.Path
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable

@Deprecated("Replaced by ShadowDrawable in the drawable subpackage")
open class ClippedShadowDrawable : ShadowDrawable {

    constructor(ownerView: View) : super(ownerView, true)

    @RequiresApi(29)
    constructor() : super(true)

    fun setPathProvider(provider: (Path) -> Unit) {
        setClipPathProvider(provider)
    }
}