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
import androidx.core.graphics.withTranslation
import com.zedalpha.shadowgadgets.view.internal.DefaultShadowColor
import com.zedalpha.shadowgadgets.view.internal.isNotDefault
import com.zedalpha.shadowgadgets.view.layer.DrawableLayer
import com.zedalpha.shadowgadgets.view.layer.Layer
import com.zedalpha.shadowgadgets.view.layer.RequiresDefaultClipLayer
import com.zedalpha.shadowgadgets.view.shadow.ClippedShadow
import com.zedalpha.shadowgadgets.view.shadow.PathProvider
import com.zedalpha.shadowgadgets.view.shadow.Shadow
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
 * The user is responsible for invalidating the current draw whenever a
 * property's value is changed. Failure to do so can result in a few different
 * possible defects, depending on the specific setup, including misaligned clip
 * regions, stale draws, etc.
 *
 * The color compat functionality is exposed here through the [colorCompat]
 * property, which is set to black by default, disabling the compat tinting.
 * Setting any non-black color enables color compat, and the [ambientColor]
 * and [spotColor] values are then ignored.
 *
 * Color compat requires an [owner] View in order to be able to hook into the
 * hardware-accelerated draw routine, and that View must be attached to the
 * onscreen hierarchy. Instances created with the @RequiresApi(29) constructor
 * simply ignore [colorCompat].
 *
 * Color compat shadows are always clipped to the drawable's bounds.
 *
 * Normally the [owner] is just the View in which the draw happens, though
 * that's not strictly necessary. However, due to a limitation in the core
 * graphics framework, the color compat mechanism needs to track its onscreen
 * location, and the lighting effects may go out of sync with the expected
 * appearance if the drawing View moves differently than the [owner].
 *
 * All ShadowDrawable instances created with an [owner] should call
 * [dispose]. This is technically not necessary for the @RequiresApi(29) ones,
 * but it is still safe to call [dispose] on those instances. Use after disposal
 * is not an automatic Exception, but it is not advised, and there is no
 * guaranteed behavior.
 *
 * The Drawable class's required [setColorFilter][Drawable.setColorFilter]
 * override is a no-op here.
 */
public open class ShadowDrawable
private constructor(
    private val shadow: Shadow,
    private val owner: View?,
    public val isClipped: Boolean
) : Drawable() {

    /**
     * The base constructor for all API levels requires an [owner] View that
     * must be attached to the onscreen hierarchy.
     *
     * [isClipped] determines whether the drawable will draw a clipped or a
     * regular shadow, the latter being useful when color compat is needed
     * without the clip. Clipped instances with irregular shapes on API levels
     * 30+ require that the outline Path be set manually through
     * [setClipPathProvider].
     *
     * It is encouraged to [dispose] of these instances when appropriate in
     * order to allow disposal of any underlying resources that may be in use.
     */
    public constructor(owner: View, isClipped: Boolean) :
            this(
                shadow = if (isClipped) ClippedShadow(owner) else Shadow(owner),
                owner = owner,
                isClipped = isClipped
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
        val clippedShadow = shadow as? ClippedShadow ?: return
        clippedShadow.pathProvider = provider?.let { PathProvider(it) }
    }

    /**
     * Releases any active internal resources.
     *
     * Use after disposal is not an automatic Exception, but it is not advised,
     * and there is no guaranteed behavior.
     */
    public fun dispose() {
        shadow.dispose()
        layer?.dispose()
    }

    /**
     * Analogous to
     * [RenderNode#setAlpha()][android.graphics.RenderNode.setAlpha],
     * but takes an Int to conform to Drawable's API.
     */
    @CallSuper
    override fun setAlpha(alpha: Int) {
        shadow.alpha = alpha / 255F
    }

    /**
     * Analogous to
     * [RenderNode#getAlpha()][android.graphics.RenderNode.getAlpha],
     * but returns an Int to conform to Drawable's API.
     */
    @CallSuper
    override fun getAlpha(): Int = (255 * shadow.alpha).roundToInt()

    /**
     * Analogous to
     * [RenderNode#getCameraDistance()][android.graphics.RenderNode.getCameraDistance]
     * and
     * [RenderNode#setCameraDistance()][android.graphics.RenderNode.setCameraDistance].
     */
    public var cameraDistance: Float
        get() = shadow.cameraDistance
        set(value) {
            shadow.cameraDistance = value
        }

    /**
     * Analogous to
     * [RenderNode#getElevation()][android.graphics.RenderNode.getElevation]
     * and
     * [RenderNode#setElevation()][android.graphics.RenderNode.setElevation].
     */
    public var elevation: Float
        get() = shadow.elevation
        set(value) {
            shadow.elevation = value
        }

    /**
     * Analogous to
     * [RenderNode#getPivotX()][android.graphics.RenderNode.getPivotX]
     * and
     * [RenderNode#setPivotX()][android.graphics.RenderNode.setPivotX].
     */
    public var pivotX: Float
        get() = shadow.pivotX
        set(value) {
            shadow.pivotX = value
        }

    /**
     * Analogous to
     * [RenderNode#getPivotY()][android.graphics.RenderNode.getPivotY]
     * and
     * [RenderNode#setPivotY()][android.graphics.RenderNode.setPivotY].
     */
    public var pivotY: Float
        get() = shadow.pivotY
        set(value) {
            shadow.pivotY = value
        }

    /**
     * Analogous to
     * [RenderNode#getRotationX()][android.graphics.RenderNode.getRotationX]
     * and
     * [RenderNode#setRotationX()][android.graphics.RenderNode.setRotationX].
     */
    public var rotationX: Float
        get() = shadow.rotationX
        set(value) {
            shadow.rotationX = value
        }

    /**
     * Analogous to
     * [RenderNode#getRotationY()][android.graphics.RenderNode.getRotationY]
     * and
     * [RenderNode#setRotationY()][android.graphics.RenderNode.setRotationY].
     */
    public var rotationY: Float
        get() = shadow.rotationY
        set(value) {
            shadow.rotationY = value
        }

    /**
     * Analogous to
     * [RenderNode#getRotationZ()][android.graphics.RenderNode.getRotationZ]
     * and
     * [RenderNode#setRotationZ()][android.graphics.RenderNode.setRotationZ].
     */
    public var rotationZ: Float
        get() = shadow.rotationZ
        set(value) {
            shadow.rotationZ = value
        }

    /**
     * Analogous to
     * [RenderNode#getScaleX()][android.graphics.RenderNode.getScaleX]
     * and
     * [RenderNode#setScaleX()][android.graphics.RenderNode.setScaleX].
     */
    public var scaleX: Float
        get() = shadow.scaleX
        set(value) {
            shadow.scaleX = value
        }

    /**
     * Analogous to
     * [RenderNode#getScaleY()][android.graphics.RenderNode.getScaleY]
     * and
     * [RenderNode#setScaleY()][android.graphics.RenderNode.setScaleY].
     */
    public var scaleY: Float
        get() = shadow.scaleY
        set(value) {
            shadow.scaleY = value
        }

    /**
     * Analogous to
     * [RenderNode#getTranslationX()][android.graphics.RenderNode.getTranslationX]
     * and
     * [RenderNode#setTranslationX()][android.graphics.RenderNode.setTranslationX].
     */
    public var translationX: Float
        get() = shadow.translationX
        set(value) {
            shadow.translationX = value
        }

    /**
     * Analogous to
     * [RenderNode#getTranslationY()][android.graphics.RenderNode.getTranslationY]
     * and
     * [RenderNode#setTranslationY()][android.graphics.RenderNode.setTranslationY].
     */
    public var translationY: Float
        get() = shadow.translationY
        set(value) {
            shadow.translationY = value
        }

    /**
     * Analogous to
     * [RenderNode#getTranslationZ()][android.graphics.RenderNode.getTranslationZ]
     * and
     * [RenderNode#setTranslationZ()][android.graphics.RenderNode.setTranslationZ].
     */
    public var translationZ: Float
        get() = shadow.translationZ
        set(value) {
            shadow.translationZ = value
        }

    private var layer: Layer? = null

    /**
     * Analogous to
     * [RenderNode#getAmbientShadowColor()][android.graphics.RenderNode.getAmbientShadowColor]
     * and
     * [RenderNode#setAmbientShadowColor()][android.graphics.RenderNode.setAmbientShadowColor].
     */
    @get:ColorInt
    @setparam:ColorInt
    public var ambientColor: Int = DefaultShadowColor
        set(value) {
            if (field == value) return
            field = value
            if (layer == null) shadow.ambientColor = value
        }

    /**
     * Analogous to
     * [RenderNode#getSpotShadowColor()][android.graphics.RenderNode.getSpotShadowColor]
     * and
     * [RenderNode#setSpotShadowColor()][android.graphics.RenderNode.setSpotShadowColor].
     */
    @get:ColorInt
    @setparam:ColorInt
    public var spotColor: Int = DefaultShadowColor
        set(value) {
            if (field == value) return
            field = value
            if (layer == null) shadow.spotColor = value
        }

    /**
     * The color that the compat mechanism uses to tint the shadow.
     * The default value is black (#FF000000), which disables the tint. If any
     * other color is set, the compat mechanism takes over, and the ambient and
     * spot values are ignored.
     *
     * Color compat requires that the drawable instance be created with a View
     * object that's attached to the onscreen hierarchy. If the View is not
     * attached to the hierarchy, there is no guaranteed behavior. If the
     * @RequiresApi(29) constructor is used, color compat is ignored.
     *
     * Color compat shadows are always clipped to the drawable's bounds.
     */
    @get:ColorInt
    @setparam:ColorInt
    public var colorCompat: Int = DefaultShadowColor
        set(value) {
            if (field == value) return
            field = value

            val owner = owner ?: return
            val shadow = shadow

            if (value.isNotDefault || isClipped && RequiresDefaultClipLayer) {
                shadow.ambientColor = DefaultShadowColor
                shadow.spotColor = DefaultShadowColor

                val layer = layer
                    ?: DrawableLayer(owner, shadow::draw, ::invalidateSelf)
                        .also { it.bounds = bounds; layer = it }

                layer.color = value
            } else {
                shadow.ambientColor = ambientColor
                shadow.spotColor = spotColor

                layer?.let { layer = null; it.dispose() }
            }

            invalidateSelf()
        }

    /**
     * Analogous to
     * [RenderNode#setOutline()][android.graphics.RenderNode.setOutline].
     */
    public fun setOutline(outline: Outline): Unit =
        shadow.setOutline(outline)

    /**
     * Analogous to
     * [RenderNode#hasIdentityMatrix()][android.graphics.RenderNode.hasIdentityMatrix].
     */
    public fun hasIdentityMatrix(): Boolean = shadow.hasIdentityMatrix()

    /**
     * Analogous to
     * [RenderNode#getMatrix()][android.graphics.RenderNode.getMatrix].
     */
    public fun getMatrix(outMatrix: Matrix): Unit =
        shadow.getMatrix(outMatrix)

    /**
     * Analogous to
     * [RenderNode#getInverseMatrix()][android.graphics.RenderNode.getInverseMatrix].
     */
    public fun getInverseMatric(outMatrix: Matrix): Unit =
        shadow.getInverseMatrix(outMatrix)

    /**
     * Flag to indicate whether the library shadow should always be composited
     * through a layer, whether or not color compat is in use.
     *
     * Addresses the issue described for
     * [View.forceShadowLayer][com.zedalpha.shadowgadgets.view.forceShadowLayer].
     *
     * This is a passive flag that should be set at initialization.
     */
    @Deprecated(
        "Forced layers are now handled automatically on the affected " +
                "versions. This property's value no longer has any effect."
    )
    public var forceLayer: Boolean = false

    @CallSuper
    override fun onBoundsChange(bounds: Rect) {
        shadow.setPosition(0, 0, bounds.width(), bounds.height())
        layer?.bounds = bounds
    }

    @CallSuper
    override fun draw(canvas: Canvas): Unit =
        layer?.draw(canvas)
            ?: canvas.withTranslation(
                x = bounds.left.toFloat(),
                y = bounds.top.toFloat(),
                block = { shadow.draw(canvas) }
            )

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT

    override fun setColorFilter(colorFilter: ColorFilter?) {}
}