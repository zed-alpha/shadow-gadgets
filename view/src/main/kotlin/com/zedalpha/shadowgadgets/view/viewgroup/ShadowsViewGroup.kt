package com.zedalpha.shadowgadgets.view.viewgroup

import androidx.annotation.ColorInt
import com.zedalpha.shadowgadgets.view.ShadowPlane

/**
 * Interface common to all current custom [ViewGroup][android.view.ViewGroup]s.
 *
 * The groups are divided into two broad categories: Recycling and Regular.
 *
 * Recycling groups:
 *   + [ShadowsRecyclerView]
 *   + [ShadowsListView]
 *   + [ShadowsExpandableListView]
 *   + [ShadowsGridView]
 *   + [ShadowsStackView]
 *
 * Regular groups:
 *   + [ShadowsChipGroup]
 *   + [ShadowsConstraintLayout]
 *   + [ShadowsCoordinatorLayout]
 *   + [ShadowsFrameLayout]
 *   + [ShadowsLinearLayout]
 *   + [ShadowsMaterialButtonToggleGroup]
 *   + [ShadowsMotionLayout]
 *   + [ShadowsRadioGroup]
 *   + [ShadowsRelativeLayout]
 *
 * The groups' main use is to allow the library's shadow properties to be set
 * on Views through layout XML. To that end, they all recognize the following
 * XML attributes that correspond to the interface properties:
 *   + `app:childShadowsPlane`
 *   + `app:clipAllChildShadows`
 *   + `app:childOutlineShadowsColorCompat`
 *   + `app:forceChildOutlineShadowsColorCompat`
 *   + `app:takeOverDrawForInlineChildShadows`
 *
 * The first four are conveniences that allow the same value to be applied to
 * each child View added during initialization.
 *
 * The last one – `takeOverDrawForInlineChildShadows` – determines whether
 * the group will take over the draw operations for
 * [Inline][com.zedalpha.shadowgadgets.view.ShadowPlane.Inline] shadows. By
 * default, Regular groups set this to `true`, so that all the planes work as
 * expected of the box; Recycling groups set it to `false`, to avoid modifying
 * the native child routine for groups that redraw rapidly.
 *
 * To help prevent confusion over the runtime behavior, these groups will set
 * their properties on children only until the group first attaches to a Window.
 * After that, the group's properties can no longer be modified, and any
 * user-added Views will not have any shadow properties set automatically.
 *
 * The Regular groups recognize the following XML attributes on child elements:
 *   + `app:shadowPlane`
 *   + `app:clipOutlineShadow`
 *   + `app:outlineShadowColorCompat`
 *   + `app:forceOutlineShadowColorCompat`
 *
 * The possible XML values correspond to the code values just as one would
 * expect. For example:
 *
 * ```xml
 * <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsFrameLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     …
 *     app:takeOverDrawForInlineChildShadows="false">
 *
 *     <Button
 *         android:id="@+id/translucent_button"
 *         …
 *         app:shadowPlane="inline"
 *         app:clipOutlineShadow="true"
 *         app:outlineShadowColorCompat="#FF0000"
 *         app:forceOutlineShadowColorCompat="true" />
 *
 * </com.zedalpha.shadowgadgets.view.viewgroup.ShadowsFrameLayout>
 * ```
 *
 * For the purposes of consistent behavior across all the different ViewGroup
 * types, these attributes will work properly only on Views with IDs that are
 * unique within their ShadowsViewGroup.
 */
public sealed interface ShadowsViewGroup {

    /**
     * The [ShadowPlane] to set on all child Views as they're added during
     * initialization.
     */
    public var childShadowsPlane: ShadowPlane

    /**
     * If `true`, sets
     * [clipOutlineShadow][com.zedalpha.shadowgadgets.view.clipOutlineShadow] to
     * `true` on all child Views as they're added during initialization.
     */
    public var clipAllChildShadows: Boolean

    /**
     * A single color compat value to set on all children added during
     * initialization.
     */
    @get:ColorInt
    @setparam:ColorInt
    public var childOutlineShadowsColorCompat: Int

    /**
     * If `true`, sets
     * [forceOutlineShadowColorCompat][com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat]
     * to `true` on all child Views added during initialization.
     */
    public var forceChildOutlineShadowsColorCompat: Boolean

    /**
     * Determines whether the group will take over the draw operations for
     * [Inline][com.zedalpha.shadowgadgets.view.ShadowPlane.Inline] shadows.
     *
     * When `true`, Inline shadows on children of the [ShadowsViewGroup] do
     * _not_ require the additional clip settings noted in
     * [Inline][com.zedalpha.shadowgadgets.view.ShadowPlane.Inline]'s docs.
     *
     * The Regular groups do this by default – i.e., they set
     * `takeOverDrawForInlineChildShadows = true` – so that all the planes work
     * as expected out of the box.
     *
     * The Recycling groups do not do this by default – i.e.,
     * `takeOverDrawForInlineChildShadows = false` – to avoid altering the
     * native routine for groups that need to redraw rapidly, and that usually
     * wouldn't need this plane anyway.
     *
     * The corresponding XML attribute always takes precedence over the one for
     * [ignoreInlineChildShadows], which this property deprecates (with opposite
     * semantics).
     */
    public var takeOverDrawForInlineChildShadows: Boolean

    /**
     * Determines whether the group will take over the draw operations for
     * [Inline][com.zedalpha.shadowgadgets.view.ShadowPlane.Inline] shadows.
     *
     * When `false`, Inline shadows on children of the [ShadowsViewGroup] do
     * _not_ require the additional clip settings noted in
     * [Inline][com.zedalpha.shadowgadgets.view.ShadowPlane.Inline]'s docs.
     *
     * The Regular groups do this by default – i.e., they set
     * `ignoreInlineChildShadows = false` – so that all the planes work
     * as expected out of the box.
     *
     * The Recycling groups do not do this by default – i.e.,
     * `ignoreInlineChildShadows = true` – to avoid altering the
     * native routine for groups that need to redraw rapidly, and that usually
     * wouldn't need this plane anyway.
     *
     * The corresponding XML attribute, which is also deprecated, is always
     * overridden by the one for [takeOverDrawForInlineChildShadows] (which has
     * opposite semantics).
     */
    @Deprecated(
        "Use takeOverDrawForInlineChildShadows " +
                "instead. It has opposite but clearer semantics."
    )
    public var ignoreInlineChildShadows: Boolean
}