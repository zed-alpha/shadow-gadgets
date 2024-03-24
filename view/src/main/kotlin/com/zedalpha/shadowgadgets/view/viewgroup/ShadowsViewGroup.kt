@file:Suppress("DEPRECATION")

package com.zedalpha.shadowgadgets.view.viewgroup

import androidx.annotation.ColorInt
import com.zedalpha.shadowgadgets.view.ClippedShadowPlane
import com.zedalpha.shadowgadgets.view.ShadowPlane

/**
 * Interface common to all current custom ViewGroups.
 *
 * The groups are divided into two broad categories: Recycling and Regular.
 *
 * Recycling groups:
 *   + ShadowsRecyclerView
 *   + ShadowsListView
 *   + ShadowsExpandableListView
 *   + ShadowsGridView
 *   + ShadowsStackView
 *
 * Regular groups:
 *   + ShadowsChipGroup
 *   + ShadowsConstraintLayout
 *   + ShadowsCoordinatorLayout
 *   + ShadowsFrameLayout
 *   + ShadowsLinearLayout
 *   + ShadowsMaterialButtonToggleGroup
 *   + ShadowsMotionLayout
 *   + ShadowsRadioGroup
 *   + ShadowsRelativeLayout
 *
 * The groups' main use is to allow the library's shadow properties to be set
 * on Views through layout XML. To that end, they all recognize the following
 * XML attributes that correspond to the interface properties:
 *   + app:childShadowsPlane
 *   + app:clipAllChildShadows
 *   + app:childOutlineShadowsColorCompat
 *   + app:forceChildOutlineShadowsColorCompat
 *   + app:ignoreInlineChildShadows
 *
 * The first four are conveniences that allow the same value to be applied to
 * each child View added during initialization.
 *
 * The last one – ignoreInlineChildShadows – determines whether
 * the group will take over the draw operations for
 * [Inline][com.zedalpha.shadowgadgets.view.ShadowPlane.Inline] shadows. By
 * default, Regular groups set this to false, so that all of the planes work as
 * expected of the box; Recycling groups set it to true, to avoid modifying the
 * native child routine for groups that redraw rapidly.
 *
 * To help prevent confusion over the runtime behavior, these groups will set
 * their properties on children only until the group first attaches to a Window.
 * After that, the group's properties can no longer be modified, and any
 * user-added Views will not have any shadow properties set automatically.
 *
 * The Recycling groups used to have specialized behavior for the handling of
 * shadow objects, but that behavior has now been expanded to work with all
 * recycling-type Views, library or not, so the Recycling label mainly just
 * serves to distinguish them from the Regular ones anymore. The only other
 * difference is that Recycling groups set ignoreInlineChildShadows to true
 * by default, as mentioned above.
 *
 * The Regular groups recognize the following XML attributes on child elements:
 *   + app:shadowPlane
 *   + app:clipOutlineShadow
 *   + app:outlineShadowColorCompat
 *   + app:forceOutlineShadowColorCompat
 *
 * The possible XML values correspond to the code values just as one would
 * expect. For example:
 *
 * ```xml
 * <com.zedalpha.shadowgadgets.view.viewgroup.ShadowsFrameLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     …
 *     app:ignoreInlineChildShadows="true">
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
 * For the purposes of consistent behavior across all of the different
 * ViewGroup types, these attributes will work properly only on Views with IDs
 * that are unique within the ViewGroup.
 */
sealed interface ShadowsViewGroup : ClippedShadowsViewGroup {

    // Overridden for dokka
    override var clipAllChildShadows: Boolean

    /**
     * Replaced by [childShadowsPlane]
     */
    @Deprecated(
        "Replaced by childShadowsPlane",
        ReplaceWith("childShadowsPlane")
    )
    override var childClippedShadowsPlane: ClippedShadowPlane

    // Overridden for dokka
    override var ignoreInlineChildShadows: Boolean

    /**
     * The ShadowPlane to set on all child Views as they're added during
     * initialization.
     */
    var childShadowsPlane: ShadowPlane

    /**
     * A single color value to set on all children during initialization as
     * their color compat value.
     */
    @get:ColorInt
    @setparam:ColorInt
    var childOutlineShadowsColorCompat: Int

    /**
     * If true, sets
     * [forceOutlineShadowColorCompat][com.zedalpha.shadowgadgets.view.forceOutlineShadowColorCompat]=true
     * on all child Views during initialization.
     */
    var forceChildOutlineShadowsColorCompat: Boolean
}

/**
 * Replaced by [ShadowsViewGroup]
 */
@Deprecated("Replaced by ShadowsViewGroup")
sealed interface ClippedShadowsViewGroup {

    /**
     * If true, sets
     * [clipOutlineShadow][com.zedalpha.shadowgadgets.view.clipOutlineShadow]=true
     * on all child Views as they're added during initialization.
     */
    var clipAllChildShadows: Boolean

    /**
     * The ClippedShadowPlane to set on all child Views as they're added during
     * initialization.
     */
    var childClippedShadowsPlane: ClippedShadowPlane

    /**
     * Determines whether the group will take over the draw operations for
     * Inline shadows.
     *
     * As direct subclasses, these groups can modify their child draw routines
     * in order to insert shadow draws between children. This allows Inline
     * shadows to be used in these groups without the additional clip settings
     * noted in
     * [the documentation][com.zedalpha.shadowgadgets.view.ShadowPlane.Inline].
     *
     * The Regular groups do this by default – i.e., they set
     * `ignoreInlineChildShadows=false` – so that all of the planes work as
     * expected out of the box.
     *
     * The Recycling groups do not do this by default – i.e.,
     * `ignoreInlineChildShadows=true` – to avoid altering the native routine
     * for groups that need to redraw rapidly, and that usually wouldn't need
     * this plane anyway.
     */
    var ignoreInlineChildShadows: Boolean
}