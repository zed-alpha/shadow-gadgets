@file:RequiresApi(Build.VERSION_CODES.LOLLIPOP)

package com.zedalpha.shadowgadgets.shadow

import android.graphics.*
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.CallSuper
import androidx.annotation.RequiresApi
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.R


internal sealed class Shadow(protected val targetView: View) {
    protected val originalProvider: ViewOutlineProvider = targetView.outlineProvider
    protected val clipPath = Path()

    val willDraw: Boolean
        get() = targetView.isVisible && !clipPath.isEmpty

    @CallSuper
    open fun attach() {
        val target = targetView
        target.outlineProvider = ProviderWrapper(originalProvider, this)
        target.shadow = this
    }

    @CallSuper
    open fun detach() {
        val target = targetView
        target.outlineProvider = originalProvider
        target.shadow = null
    }

    @CallSuper
    @Suppress("LiftReturnOrAssignment")
    open fun setOutline(outline: Outline) {
        val path = clipPath
        path.reset()

        if (outline.isEmpty) return

        val outlineBounds = CacheRect
        if (getOutlineRect(outline, outlineBounds)) {
            val boundsF = CacheRectF
            boundsF.set(outlineBounds)
            val outlineRadius = getOutlineRadius(outline)
            path.addRoundRect(boundsF, outlineRadius, outlineRadius, Path.Direction.CW)
        } else {
            setOutlinePath(outline, path)
        }
    }

    abstract fun update(): Boolean
    abstract fun show()
    abstract fun hide()
}

internal var View.shadow: Shadow?
    get() = getTag(R.id.tag_target_shadow) as? Shadow
    set(value) {
        setTag(R.id.tag_target_shadow, value)
    }

private class ProviderWrapper(
    private val wrapped: ViewOutlineProvider,
    private val shadow: Shadow
) : ViewOutlineProvider() {
    override fun getOutline(view: View, outline: Outline) {
        wrapped.getOutline(view, outline)
        shadow.setOutline(outline)
        outline.alpha = 0.0F
    }
}

private val CacheRect = Rect()
private val CacheRectF = RectF()

internal val CachePath = Path()
internal val CacheMatrix = Matrix()