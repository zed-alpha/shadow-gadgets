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

/**
 * This class is a thin wrapper around the library's core draw functionalities,
 * allowing its shadows to be drawn manually without having to use the core
 * module directly.
 *
 * All ShadowDrawable instances created with an [ownerView] should call
 * [dispose]. This is technically not necessary for the @RequiresApi(29) ones,
 * but it is still safe to call [dispose] on those instances. Use after disposal
 * is not an automatic Exception, but it is not advised, and there is no
 * guaranteed behavior.
 *
 * The user is responsible for invalidating the current draw whenever a
 * property's value is changed. Failure to do so can result in a few different
 * possible defects, depending on the specific setup, including misaligned clip
 * regions, stale draws, etc.
 *
 * This drawable's bounds do not affect the shadow's size, shape, or position.
 * Those are initialized from the properties of the Outline set with
 * [setOutline], and are able to be modified afterward with the relevant
 * functions and properties; e.g, [setPosition], [translationX], [scaleY], etc.
 *
 * The color compat functionality is exposed here through the [colorCompat]
 * property, which is set to black by default, disabling the compat tinting.
 * Setting any non-black color enables color compat, and the [ambientColor]
 * and [spotColor] values are then ignored.
 *
 * The color compat functionality requires a View object. Instances created with
 * the @RequiresApi(29) constructor simply ignore [colorCompat].
 *
 * The Drawable class's required [setColorFilter][Drawable.setColorFilter]
 * override is a no-op.
 *
 * When using color compat, the shadows are clipped to the drawable's bounds.
 * Also, due to differences in the native framework, all shadows on API levels
 * 24..28 are clipped to the drawable's bounds.
 */
open class ShadowDrawable private constructor(
    internal val coreShadow: Shadow,
    private val ownerView: View?,
    val isClipped: Boolean
) : Drawable() {

    /**
     * The base constructor for all API levels requires an [ownerView] in order
     * to be able to hook into the hardware-accelerated draw routine.
     *
     * This View must be attached to the on-screen hierarchy; usually, it's just
     * the one in which the draw happens.
     *
     * [isClipped] determines whether the drawable will draw a clipped or a
     * regular shadow, the latter being useful when color compat is needed
     * without the clipping.
     *
     * It's rather important to [dispose] of these instances when appropriate.
     */
    constructor(ownerView: View, isClipped: Boolean) : this(
        if (isClipped) ClippedShadow(ownerView) else Shadow(ownerView),
        ownerView,
        isClipped
    )

    /**
     * At API level 29, an owner View is not required, but color compat is
     * unavailable for these instances.
     *
     * [isClipped] determines whether the drawable will draw a clipped or a
     * regular shadow, the latter being useful when color compat is needed
     * without the clipping.
     *
     * It is not necessary to call [dispose] on these instances, but it is safe
     * to do so.
     */
    @RequiresApi(29)
    constructor(isClipped: Boolean) :
            this(if (isClipped) ClippedShadow() else Shadow(), null, isClipped)

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
    fun setClipPathProvider(provider: ((Path) -> Unit)?) {
        val clippedShadow = coreShadow as? ClippedShadow ?: return
        clippedShadow.pathProvider = provider?.let { PathProvider(it) }
    }

    /**
     * Releases any active internal resources.
     *
     * Use after disposal is not an automatic Exception, but it is not advised,
     * and there is no guaranteed behavior.
     */
    fun dispose() {
        coreShadow.dispose()
        layer?.dispose()
    }

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
            if (layer == null) {
                coreShadow.ambientColor = value
            }
        }

    @get:ColorInt
    @setparam:ColorInt
    var spotColor: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value
            if (layer == null) {
                coreShadow.spotColor = value
            }
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