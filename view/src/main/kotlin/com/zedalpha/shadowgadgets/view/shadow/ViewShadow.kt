package com.zedalpha.shadowgadgets.view.shadow

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.CallSuper
import androidx.core.view.isVisible
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.ViewShadowColorsHelper
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.layer.RequiresDefaultClipLayer
import com.zedalpha.shadowgadgets.view.R
import com.zedalpha.shadowgadgets.view.clipOutlineShadow
import com.zedalpha.shadowgadgets.view.colorOutlineShadow
import com.zedalpha.shadowgadgets.view.forceShadowLayer
import com.zedalpha.shadowgadgets.view.pathProvider

internal abstract class ViewShadow(
    internal val targetView: View,
    private val controller: ShadowController?
) {
    var isShown: Boolean = true

    val isClipped: Boolean = targetView.clipOutlineShadow

    protected val coreShadow = if (isClipped) {
        ClippedShadow(targetView).also { shadow ->
            val pathProvider = targetView.pathProvider ?: return@also
            shadow.pathProvider = PathProvider { path ->
                pathProvider.getPath(targetView, path)
            }
        }
    } else {
        Shadow(targetView)
    }

    protected var coreLayer: Layer? = null

    init {
        @Suppress("LeakingThis")
        targetView.shadow = this
    }

    protected val provider: ViewOutlineProvider = targetView.outlineProvider

    protected fun wrapOutlineProvider(setOutline: (Outline) -> Unit) {
        targetView.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                provider.getOutline(view, outline)
                setOutline.invoke(outline)
                outline.alpha = 0F
            }
        }
    }

    @CallSuper
    open fun detachFromTarget() {
        (coreShadow as? ClippedShadow)?.pathProvider = null
        coreShadow.dispose()

        coreLayer?.let { controller?.disposeLayer(it) }

        targetView.outlineProvider = provider
        targetView.shadow = null
    }

    fun updateColorCompat(color: Int) {
        val view = targetView
        val needsLayer = view.colorOutlineShadow ||
                (isClipped && RequiresDefaultClipLayer) ||
                @Suppress("DEPRECATION") view.forceShadowLayer
        if (needsLayer) {
            if (Build.VERSION.SDK_INT >= 28) {
                with(ViewShadowColorsHelper) {
                    setSpotColor(view, DefaultShadowColorInt)
                    setAmbientColor(view, DefaultShadowColorInt)
                }
            }

            val layer = coreLayer
                ?: controller?.obtainLayer(coreShadow)
                    .also { coreLayer = it }
            layer?.color = color
        } else {
            coreLayer?.let {
                controller?.disposeLayer(it)
                coreLayer = null
            }
        }
        invalidate()
    }

    abstract fun invalidate()

    protected fun View.updateAndCheckDraw(shadow: Shadow): Boolean {
        shadow.setPosition(left, top, right, bottom)
        shadow.alpha = alpha
        shadow.cameraDistance = cameraDistance
        shadow.elevation = elevation
        shadow.pivotX = pivotX
        shadow.pivotY = pivotY
        shadow.rotationX = rotationX
        shadow.rotationY = rotationY
        shadow.rotationZ = rotation
        shadow.scaleX = scaleX
        shadow.scaleY = scaleY
        shadow.translationX = translationX
        shadow.translationY = translationY
        shadow.translationZ = translationZ
        if (Build.VERSION.SDK_INT >= 28) {
            shadow.ambientColor = ViewShadowColorsHelper.getAmbientColor(this)
            shadow.spotColor = ViewShadowColorsHelper.getSpotColor(this)
        }
        return isVisible && elevation > 0F
    }
}

internal var View.shadow: ViewShadow?
    get() = getTag(R.id.shadow) as? ViewShadow
    private set(value) = setTag(R.id.shadow, value)