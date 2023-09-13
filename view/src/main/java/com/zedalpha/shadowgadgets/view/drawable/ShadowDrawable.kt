package com.zedalpha.shadowgadgets.view.drawable

import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.Matrix
import android.graphics.Outline
import android.graphics.Path
import android.graphics.PixelFormat
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi
import com.zedalpha.shadowgadgets.core.ClippedShadow
import com.zedalpha.shadowgadgets.core.DefaultShadowColorInt
import com.zedalpha.shadowgadgets.core.PathProvider
import com.zedalpha.shadowgadgets.core.Shadow
import com.zedalpha.shadowgadgets.core.layer.VersionRequiresDefaultSoloLayer
import kotlin.math.roundToInt

open class ShadowDrawable private constructor(
    internal val coreShadow: Shadow,
    private val ownerView: View?,
    val isClipped: Boolean
) : Drawable() {

    constructor(ownerView: View, isClipped: Boolean) : this(
        if (isClipped) ClippedShadow(ownerView) else Shadow(ownerView),
        ownerView,
        isClipped
    )

    @RequiresApi(29)
    constructor(isClipped: Boolean) :
            this(if (isClipped) ClippedShadow() else Shadow(), null, isClipped)

    @CallSuper
    override fun setAlpha(alpha: Int) {
        coreShadow.alpha = alpha / 255F
    }

    @CallSuper
    override fun getAlpha(): Int {
        return (255 * coreShadow.alpha).roundToInt()
    }

    var cameraDistance: Float
        get() = coreShadow.cameraDistance
        set(value) {
            coreShadow.cameraDistance = value
        }

    var elevation: Float
        get() = coreShadow.elevation
        set(value) {
            coreShadow.elevation = value
        }

    var pivotX: Float
        get() = coreShadow.pivotX
        set(value) {
            coreShadow.pivotX = value
        }

    var pivotY: Float
        get() = coreShadow.pivotY
        set(value) {
            coreShadow.pivotY = value
        }

    var rotationX: Float
        get() = coreShadow.rotationX
        set(value) {
            coreShadow.rotationX = value
        }

    var rotationY: Float
        get() = coreShadow.rotationY
        set(value) {
            coreShadow.rotationY = value
        }

    var rotationZ: Float
        get() = coreShadow.rotationZ
        set(value) {
            coreShadow.rotationZ = value
        }

    var scaleX: Float
        get() = coreShadow.scaleX
        set(value) {
            coreShadow.scaleX = value
        }

    var scaleY: Float
        get() = coreShadow.scaleY
        set(value) {
            coreShadow.scaleY = value
        }

    var translationX: Float
        get() = coreShadow.translationX
        set(value) {
            coreShadow.translationX = value
        }

    var translationY: Float
        get() = coreShadow.translationY
        set(value) {
            coreShadow.translationY = value
        }

    var translationZ: Float
        get() = coreShadow.translationZ
        set(value) {
            coreShadow.translationZ = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var ambientColor: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value
            if (layer == null) coreShadow.ambientColor = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var spotColor: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value
            if (layer == null) coreShadow.spotColor = value
        }

    @get:ColorInt
    @setparam:ColorInt
    var colorCompat: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value
            configureLayer()
        }

    fun setPosition(left: Int, top: Int, right: Int, bottom: Int) {
        coreShadow.setPosition(left, top, right, bottom)
    }

    fun setOutline(outline: Outline) {
        coreShadow.setOutline(outline)
    }

    fun hasIdentityMatrix(): Boolean =
        coreShadow.hasIdentityMatrix()

    fun getMatrix(outMatrix: Matrix) {
        coreShadow.getMatrix(outMatrix)
    }

    fun setClipPathProvider(provider: ((Path) -> Unit)?) {
        val clippedShadow = coreShadow as? ClippedShadow ?: return
        clippedShadow.pathProvider = provider?.let { PathProvider(it) }
    }

    var forceLayer = false

    private var layer: SoloLayer? = null

    init {
        configureLayer()
    }

    private fun configureLayer() {
        val owner = ownerView
        val shadow = coreShadow
        val compat = colorCompat
        val needsLayer = compat != DefaultShadowColorInt || forceLayer ||
                VersionRequiresDefaultSoloLayer
        if (needsLayer && owner != null) {
            shadow.ambientColor = DefaultShadowColorInt
            shadow.spotColor = DefaultShadowColorInt
            layer?.run { color = compat; return }
            layer = SoloLayer(this, owner, shadow::draw, compat)
        } else {
            shadow.ambientColor = ambientColor
            shadow.spotColor = spotColor
            layer?.run { dispose(); layer = null }
        }
    }

    fun dispose() {
        coreShadow.dispose()
        layer?.dispose()
    }

    @CallSuper
    override fun onBoundsChange(bounds: Rect) {
        layer?.setSize(bounds)
    }

    @CallSuper
    override fun draw(canvas: Canvas) {
        layer?.draw(canvas) ?: coreShadow.draw(canvas)
    }

    @CallSuper
    override fun invalidateSelf() {
        super.invalidateSelf()
        layer?.refresh()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}