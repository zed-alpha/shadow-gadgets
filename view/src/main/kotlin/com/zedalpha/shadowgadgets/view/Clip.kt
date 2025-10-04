package com.zedalpha.shadowgadgets.view

import android.graphics.Path
import android.graphics.drawable.Drawable
import android.view.View
import com.google.android.material.shape.MaterialShapeDrawable
import com.zedalpha.shadowgadgets.view.internal.findMaterialShapeDrawable
import com.zedalpha.shadowgadgets.view.internal.setFromMaterialShapeDrawable
import com.zedalpha.shadowgadgets.view.internal.updateShadow
import com.zedalpha.shadowgadgets.view.internal.viewTag
import com.zedalpha.shadowgadgets.view.plane.updatePlane
import com.zedalpha.shadowgadgets.view.proxy.shadowProxy

/**
 * The current state of the clipped shadow fix for the receiver View.
 *
 * While this feature is active, the receiver's
 * [ViewOutlineProvider][android.view.ViewOutlineProvider] is wrapped
 * in a custom library implementation. Any user implementations should be set
 * before enabling this feature, or at least before the View attaches to its
 * Window.
 *
 * When `true`, the View's intrinsic shadow is always disabled, even if the
 * replacement cannot be drawn, for whatever reason.
 */
public var View.clipOutlineShadow: Boolean
        by viewTag(R.id.clip_outline_shadow, false) {
            updateShadow(this)
            this.shadowProxy?.let { proxy ->
                proxy.updateClip()
                updatePlane(proxy)
            }
        }

/**
 * An interface through which to set the clip [Path] for irregularly shaped
 * Views on API levels 30 and above.
 *
 * Views that are not circles, plain rectangles, or single-radius rounded
 * rectangles have their shapes defined by a Path field that became inaccessible
 * starting with Android R. For those cases, this interface and its
 * corresponding extension property – [pathProvider] – provide a fallback
 * through which to set it manually.
 */
public fun interface ViewPathProvider {

    /**
     * Called for non-rectangular shapes on API levels 30 and above.
     *
     * The [view] is the target itself, and the [path] is an empty instance that
     * should be set appropriately. If the Path is left empty, no shadow will be
     * drawn.
     */
    public fun getPath(view: View, path: Path)
}

/**
 * The [ViewPathProvider] used by [clipOutlineShadow] for irregular shapes on
 * API levels 30+.
 */
public var View.pathProvider: ViewPathProvider?
        by viewTag(R.id.path_provider, null) {
            if (this.clipOutlineShadow) this.shadowProxy?.updatePathProvider()
        }

/**
 * An implementation of [ViewPathProvider] that tries to set the [Path] from a
 * [MaterialShapeDrawable] in the target's background.
 *
 * If a MaterialShapeDrawable cannot be found, the Path is left unchanged.
 */
@Deprecated(
    "Replaced with more performant MaterialShapeDrawableViewPathProvider",
    ReplaceWith("MaterialShapeDrawableViewPathProvider()")
)
public val MaterialComponentsViewPathProvider: ViewPathProvider =
    ViewPathProvider { view, path ->
        view.background?.findMaterialShapeDrawable()?.let { msd ->
            path.setFromMaterialShapeDrawable(msd)
        }
    }

/**
 * An implementation of [ViewPathProvider] that tries to set the [Path] from a
 * [MaterialShapeDrawable] in the target's background.
 *
 * If a MaterialShapeDrawable cannot be found, the Path is left unchanged.
 */
public class MaterialShapeDrawableViewPathProvider : ViewPathProvider {

    private var background: Drawable? = null

    private var msd: MaterialShapeDrawable? = null

    override fun getPath(view: View, path: Path) {
        val current: Drawable? = view.background
        val msd =
            if (background !== current) {
                background = current
                view.shadowProxy?.invalidate()
                current?.findMaterialShapeDrawable().also { msd = it }
            } else {
                msd
            }
        msd ?: return
        path.setFromMaterialShapeDrawable(msd)
    }

    public companion object {

        /**
         * Indicates whether [MaterialShapeDrawableViewPathProvider] is able
         * to read and copy a [MaterialShapeDrawable]'s private outline [Path].
         */
        @Deprecated(
            "The method used to obtain the Path no longer involves " +
                    "reflection. From now on, this will always true."
        )
        public val canGetPath: Boolean = true

        /**
         * Returns the first [MaterialShapeDrawable] found if [root] is or
         * contains one.
         *
         * This is provided as a convenience to help ensure that
         * [MaterialShapeDrawableViewPathProvider] will work correctly with a
         * given [Drawable] at runtime.
         */
        public fun findMaterialShapeDrawable(root: Drawable): MaterialShapeDrawable? =
            root.findMaterialShapeDrawable()
    }
}

/**
 * A patch fix for potential clip defects on API levels 24..28, when a target's
 * parent ViewGroup has a non-identity matrix applied.
 *
 * This is a passive flag that should be set during initialization. Modifying
 * its value while a library shadow is active will not automatically update that
 * instance.
 *
 * More information is available on
 * [this wiki page](https://github.com/zed-alpha/shadow-gadgets/wiki/View.forceShadowLayer).
 */
@Deprecated(
    "Forced layers are now handled automatically on the affected " +
            "versions. This property's value no longer has any effect."
)
public var View.forceShadowLayer: Boolean
        by viewTag(R.id.force_shadow_layer, false)