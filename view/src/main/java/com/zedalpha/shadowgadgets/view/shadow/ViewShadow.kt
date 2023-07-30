package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.CallSuper
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.ShadowColorFilter
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.outlineShadowColorCompat
import com.zedalpha.shadowgadgets.view.pathProvider
import com.zedalpha.shadowgadgets.view.requiresColor


internal abstract class ViewShadow(protected val targetView: View) {

    protected val shadow = targetView.run {
        if (clipOutlineShadow) {
            ClippedShadow(this, { pathProvider?.getPath(this, it) })
        } else {
            Shadow(this)
        }
    }

    val colorFilter = ShadowColorFilter().apply {
        color = targetView.outlineShadowColorCompat
    }

    val requiresColor get() = targetView.requiresColor

    val filterColor get() = colorFilter.color

    private val provider: ViewOutlineProvider = targetView.outlineProvider

    init {
        @Suppress("LeakingThis")
        targetView.shadow = this
        targetView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                provider.getOutline(view, outline)
                shadow.setOutline(outline)
                outline.alpha = 0.0F
            }
        }
    }

    @CallSuper
    open fun detachFromTarget() {
        targetView.shadow = null
        targetView.outlineProvider = provider
        shadow.dispose()
    }

    fun checkRecreate(): Boolean =
        targetView.clipOutlineShadow != shadow is ClippedShadow

    open fun show() {}

    open fun hide() {}

    open fun updateFilter(color: Int) {}

    open fun invalidate() {}
}

internal var View.shadow: ViewShadow?
    get() = getTag(R.id.shadow) as? ViewShadow
    private set(value) = setTag(R.id.shadow, value)