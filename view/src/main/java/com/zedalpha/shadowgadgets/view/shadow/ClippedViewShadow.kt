package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.pathProvider


internal abstract class ClippedViewShadow(protected val targetView: View) {

    protected val clippedShadow = ClippedShadow.newInstance(targetView)

    protected val provider: ViewOutlineProvider = targetView.outlineProvider

    init {
        @Suppress("LeakingThis")
        targetView.clippedShadow = this

        // pathProvider must be set first. Setting outlineProvider fires
        // getOutline() immediately, and if the target View never invalidates
        // again, it'll never run again to pull a PathProvider set afterward.
        clippedShadow.pathProvider = PathProvider { path ->
            targetView.pathProvider?.getPath(targetView, path)
        }
        targetView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                provider.getOutline(view, outline)
                clippedShadow.setOutline(outline)
                outline.alpha = 0.0F
            }
        }
    }

    @CallSuper
    open fun detachFromTarget() {
        targetView.clippedShadow = null
        targetView.outlineProvider = provider
        clippedShadow.dispose()
    }

    open fun show() {}

    open fun hide() {}
}

internal var View.clippedShadow: ClippedViewShadow?
    get() = getTag(R.id.clipped_shadow) as? ClippedViewShadow
    private set(value) = setTag(R.id.clipped_shadow, value)