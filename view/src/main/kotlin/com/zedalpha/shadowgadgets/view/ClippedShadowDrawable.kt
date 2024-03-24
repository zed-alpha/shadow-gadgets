package com.zedalpha.shadowgadgets.view

import android.graphics.Path
import android.view.View
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable

/**
 * Replaced by [ShadowDrawable] in the drawable subpackage
 */
@Deprecated(
    "Replaced by ShadowDrawable",
    ReplaceWith(
        "ShadowDrawable",
        "com.zedalpha.shadowgadgets.view.drawable.ShadowDrawable"
    )
)
open class ClippedShadowDrawable : ShadowDrawable {

    /**
     * The base constructor for all API levels requires an [ownerView] in order
     * to be able to hook into the hardware-accelerated draw routine.
     *
     * It's rather important to [dispose][ShadowDrawable.dispose] of these
     * instances when appropriate.
     */
    constructor(ownerView: View) : super(ownerView, true)

    /**
     * At API level 29, no View is needed.
     *
     * It is not necessary to call [dispose][ShadowDrawable.dispose] on
     * these instances, but it is safe to do so.
     */
    @RequiresApi(29)
    constructor() : super(true)

    /**
     * Sets the function through which to provide irregular Paths for clipping
     * on API levels 30 and above.
     *
     * The Path passed into the [provider] function is expected to be set to the
     * appropriate shape. If it's left empty, the shadow will not be drawn.
     *
     * Analogous to setting a target View's
     * [ViewPathProvider][com.zedalpha.shadowgadgets.view.ViewPathProvider].
     */
    fun setPathProvider(provider: (Path) -> Unit) {
        setClipPathProvider(provider)
    }
}