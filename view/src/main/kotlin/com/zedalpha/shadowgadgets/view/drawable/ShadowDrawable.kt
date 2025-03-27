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
import com.zedalpha.shadowgadgets.core.isNotDefault
import com.zedalpha.shadowgadgets.core.layer.Layer
import com.zedalpha.shadowgadgets.core.layer.RequiresDefaultClipLayer
import kotlin.math.roundToInt

/**
 * This class is a thin wrapper around the library's core draw functionalities,
 * allowing its shadows to be drawn manually without having to use the core
 * module directly.
 *
 * Clipped instances with irregular shapes on API levels 30+ must have the
 * outline Path set manually using the [setClipPathProvider] function. This is
 * analogous to setting a
 * [ViewPathProvider][com.zedalpha.shadowgadgets.view.ViewPathProvider] on a
 * target View.
 *
 * This drawable suffers from the same potential clip alignment glitch that the
 * View solution does on API levels 24..28. The [forceLayer] property is
 * available to address the issue as described for
 * [View.forceShadowLayer][com.zedalpha.shadowgadgets.view.forceShadowLayer].
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
 * This drawable's bounds do not affect the shadow's size or shape. Those are
 * initialized from the properties of the Outline passed to [setOutline], and
 * are modifiable afterward with the relevant functions and properties;
 * e.g, [setPosition], [translationX], [scaleY], etc.
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
 * Also, due to differences in the native framework, all clipped shadows on API
 * levels 24..28 are clipped to the drawable's bounds too, whether color or not.
 */
public open class ShadowDrawable private constructor(
    private val coreShadow: Shadow,
    private val ownerView: View?,
    public val isClipped: Boolean
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
     * without the clip. Clipped instances with irregular shapes on API levels
     * 30+ require that the outline Path be set manually through
     * [setClipPathProvider].
     *
     * It is rather important to [dispose] of these instances when appropriate.
     */
    public constructor(ownerView: View, isClipped: Boolean) : this(
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
     * without the clip. Clipped instances with irregular shapes on API levels
     * 30+ require that the outline Path be set manually through
     * [setClipPathProvider].
     *
     * It is not necessary to call [dispose] on these instances, but it is safe
     * to do so.
     */
    @RequiresApi(29)
    public constructor(isClipped: Boolean) :
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
    public fun setClipPathProvider(provider: ((Path) -> Unit)?) {
        val clippedShadow = coreShadow as? ClippedShadow ?: return
        clippedShadow.pathProvider = provider?.let { PathProvider(it) }
    }

    /**
     * Releases any active internal resources.
     *
     * Use after disposal is not an automatic Exception, but it is not advised,
     * and there is no guaranteed behavior.
     */
    public fun dispose() {
        coreShadow.dispose()
        coreLayer?.dispose()
    }

    /**
     * Analogous to
     * [RenderNode#setAlpha()][android.graphics.RenderNode.setAlpha],
     * but takes an Int to conform to Drawable's API.
     */
    @CallSuper
    override fun setAlpha(alpha: Int) {
        coreShadow.alpha = alpha / 255F
    }

    /**
     * Analogous to
     * [RenderNode#getAlpha()][android.graphics.RenderNode.getAlpha],
     * but returns an Int to conform to Drawable's API.
     */
    @CallSuper
    override fun getAlpha(): Int = (255 * coreShadow.alpha).roundToInt()

    /**
     * Analogous to
     * [RenderNode#getCameraDistance()][android.graphics.RenderNode.getCameraDistance]
     * and
     * [RenderNode#setCameraDistance()][android.graphics.RenderNode.setCameraDistance].
     */
    public var cameraDistance: Float
        get() = coreShadow.cameraDistance
        set(value) {
            coreShadow.cameraDistance = value
        }

    /**
     * Analogous to
     * [RenderNode#getElevation()][android.graphics.RenderNode.getElevation]
     * and
     * [RenderNode#setElevation()][android.graphics.RenderNode.setElevation].
     */
    public var elevation: Float
        get() = coreShadow.elevation
        set(value) {
            coreShadow.elevation = value
        }

    /**
     * Analogous to
     * [RenderNode#getPivotX()][android.graphics.RenderNode.getPivotX]
     * and
     * [RenderNode#setPivotX()][android.graphics.RenderNode.setPivotX].
     */
    public var pivotX: Float
        get() = coreShadow.pivotX
        set(value) {
            coreShadow.pivotX = value
        }

    /**
     * Analogous to
     * [RenderNode#getPivotY()][android.graphics.RenderNode.getPivotY]
     * and
     * [RenderNode#setPivotY()][android.graphics.RenderNode.setPivotY].
     */
    public var pivotY: Float
        get() = coreShadow.pivotY
        set(value) {
            coreShadow.pivotY = value
        }

    /**
     * Analogous to
     * [RenderNode#getRotationX()][android.graphics.RenderNode.getRotationX]
     * and
     * [RenderNode#setRotationX()][android.graphics.RenderNode.setRotationX].
     */
    public var rotationX: Float
        get() = coreShadow.rotationX
        set(value) {
            coreShadow.rotationX = value
        }

    /**
     * Analogous to
     * [RenderNode#getRotationY()][android.graphics.RenderNode.getRotationY]
     * and
     * [RenderNode#setRotationY()][android.graphics.RenderNode.setRotationY].
     */
    public var rotationY: Float
        get() = coreShadow.rotationY
        set(value) {
            coreShadow.rotationY = value
        }

    /**
     * Analogous to
     * [RenderNode#getRotationZ()][android.graphics.RenderNode.getRotationZ]
     * and
     * [RenderNode#setRotationZ()][android.graphics.RenderNode.setRotationZ].
     */
    public var rotationZ: Float
        get() = coreShadow.rotationZ
        set(value) {
            coreShadow.rotationZ = value
        }

    /**
     * Analogous to
     * [RenderNode#getScaleX()][android.graphics.RenderNode.getScaleX]
     * and
     * [RenderNode#setScaleX()][android.graphics.RenderNode.setScaleX].
     */
    public var scaleX: Float
        get() = coreShadow.scaleX
        set(value) {
            coreShadow.scaleX = value
        }

    /**
     * Analogous to
     * [RenderNode#getScaleY()][android.graphics.RenderNode.getScaleY]
     * and
     * [RenderNode#setScaleY()][android.graphics.RenderNode.setScaleY].
     */
    public var scaleY: Float
        get() = coreShadow.scaleY
        set(value) {
            coreShadow.scaleY = value
        }

    /**
     * Analogous to
     * [RenderNode#getTranslationX()][android.graphics.RenderNode.getTranslationX]
     * and
     * [RenderNode#setTranslationX()][android.graphics.RenderNode.setTranslationX].
     */
    public var translationX: Float
        get() = coreShadow.translationX
        set(value) {
            coreShadow.translationX = value
        }

    /**
     * Analogous to
     * [RenderNode#getTranslationY()][android.graphics.RenderNode.getTranslationY]
     * and
     * [RenderNode#setTranslationY()][android.graphics.RenderNode.setTranslationY].
     */
    public var translationY: Float
        get() = coreShadow.translationY
        set(value) {
            coreShadow.translationY = value
        }

    /**
     * Analogous to
     * [RenderNode#getTranslationZ()][android.graphics.RenderNode.getTranslationZ]
     * and
     * [RenderNode#setTranslationZ()][android.graphics.RenderNode.setTranslationZ].
     */
    private var translationZ: Float
        get() = coreShadow.translationZ
        set(value) {
            coreShadow.translationZ = value
        }

    /**
     * Analogous to
     * [RenderNode#getAmbientShadowColor()][android.graphics.RenderNode.getAmbientShadowColor]
     * and
     * [RenderNode#setAmbientShadowColor()][android.graphics.RenderNode.setAmbientShadowColor].
     */
    @get:ColorInt
    @setparam:ColorInt
    public var ambientColor: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value
            if (coreLayer == null) coreShadow.ambientColor = value
        }

    /**
     * Analogous to
     * [RenderNode#getSpotShadowColor()][android.graphics.RenderNode.getSpotShadowColor]
     * and
     * [RenderNode#setSpotShadowColor()][android.graphics.RenderNode.setSpotShadowColor].
     */
    @get:ColorInt
    @setparam:ColorInt
    public var spotColor: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value
            if (coreLayer == null) coreShadow.spotColor = value
        }

    /**
     * The color that the compat mechanism uses to tint the shadow.
     * The default value is black (#FF000000), which disables the tint. If any
     * other color is set, the compat mechanism takes over, and the ambient and
     * spot values are ignored.
     *
     * Color compat requires that the drawable instance be created with a View
     * object that's attached to the on-screen hierarchy. If the View is not
     * attached to the hierarchy, there is no guaranteed behavior. If the
     * @Requires29 constructor is used, color compat is ignored.
     *
     * Color compat shadows are always clipped to the drawable's bounds.
     */
    @get:ColorInt
    @setparam:ColorInt
    public var colorCompat: Int = DefaultShadowColorInt
        set(value) {
            if (field == value) return
            field = value

            val needsLayer = colorCompat.isNotDefault ||
                    (isClipped && RequiresDefaultClipLayer) ||
                    @Suppress("DEPRECATION") forceLayer
            if (needsLayer) {
                val shadow = coreShadow
                shadow.ambientColor = DefaultShadowColorInt
                shadow.spotColor = DefaultShadowColorInt

                val layer = coreLayer
                    ?: controller.obtainLayer(shadow).also { coreLayer = it }
                layer?.color = value
            } else {
                coreLayer?.let {
                    controller.disposeLayer(it)
                    coreLayer = null
                }
            }
            invalidateSelf()
        }

    private val controller = DrawableController(this, ownerView)

    private var coreLayer: Layer? = null

    /**
     * Analogous to
     * [RenderNode#setPosition()][android.graphics.RenderNode.setPosition].
     */
    public fun setPosition(left: Int, top: Int, right: Int, bottom: Int): Unit =
        coreShadow.setPosition(left, top, right, bottom)

    /**
     * Analogous to
     * [RenderNode#setOutline()][android.graphics.RenderNode.setOutline].
     */
    public fun setOutline(outline: Outline): Unit =
        coreShadow.setOutline(outline)

    /**
     * Analogous to
     * [RenderNode#hasIdentityMatrix()][android.graphics.RenderNode.hasIdentityMatrix].
     */
    public fun hasIdentityMatrix(): Boolean = coreShadow.hasIdentityMatrix()

    /**
     * Analogous to
     * [RenderNode#getMatrix()][android.graphics.RenderNode.getMatrix].
     */
    public fun getMatrix(outMatrix: Matrix): Unit =
        coreShadow.getMatrix(outMatrix)

    /**
     * Flag to indicate whether the library shadow should always be composited
     * through a layer, whether or not color compat is in use.
     *
     * Addresses the issue described for
     * [View.forceShadowLayer][com.zedalpha.shadowgadgets.view.forceShadowLayer].
     *
     * This is a passive flag that should be set at initialization.
     */
    @Deprecated("This is now handled automatically on the affected versions.")
    public var forceLayer: Boolean = false

    @CallSuper
    override fun onBoundsChange(bounds: Rect) {
        coreLayer?.setSize(bounds.width(), bounds.height())
    }

    @CallSuper
    override fun draw(canvas: Canvas): Unit =
        coreLayer?.draw(canvas) ?: coreShadow.draw(canvas)

    @CallSuper
    override fun invalidateSelf() {
        super.invalidateSelf()
        coreLayer?.refresh()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}