package com.zedalpha.shadowgadgets.view.lint.internal

internal const val SHADOWS_CHIP_GROUP =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsChipGroup"
internal const val SHADOWS_CONSTRAINT_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsConstraintLayout"
internal const val SHADOWS_COORDINATOR_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsCoordinatorLayout"
internal const val SHADOWS_EXPANDABLE_LIST_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsExpandableListView"
internal const val SHADOWS_FRAME_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsFrameLayout"
internal const val SHADOWS_GRID_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsGridView"
internal const val SHADOWS_LINEAR_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsLinearLayout"
internal const val SHADOWS_MATERIAL_BUTTON_TOGGLE_GROUP =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMaterialButtonToggleGroup"
internal const val SHADOWS_LIST_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsListView"
internal const val SHADOWS_MOTION_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsMotionLayout"
internal const val SHADOWS_RADIO_GROUP =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRadioGroup"
internal const val SHADOWS_RECYCLER_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRecyclerView"
internal const val SHADOWS_RELATIVE_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsRelativeLayout"
internal const val SHADOWS_STACK_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ShadowsStackView"

internal val ALL_SHADOWS_VIEW_GROUPS = setOf(
    SHADOWS_CHIP_GROUP,
    SHADOWS_CONSTRAINT_LAYOUT,
    SHADOWS_COORDINATOR_LAYOUT,
    SHADOWS_EXPANDABLE_LIST_VIEW,
    SHADOWS_FRAME_LAYOUT,
    SHADOWS_GRID_VIEW,
    SHADOWS_LINEAR_LAYOUT,
    SHADOWS_LIST_VIEW,
    SHADOWS_MATERIAL_BUTTON_TOGGLE_GROUP,
    SHADOWS_MOTION_LAYOUT,
    SHADOWS_RADIO_GROUP,
    SHADOWS_RECYCLER_VIEW,
    SHADOWS_RELATIVE_LAYOUT,
    SHADOWS_STACK_VIEW
)

internal const val CLIPPED_SHADOWS_CHIP_GROUP =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsChipGroup"
internal const val CLIPPED_SHADOWS_CONSTRAINT_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsConstraintLayout"
internal const val CLIPPED_SHADOWS_COORDINATOR_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsCoordinatorLayout"
internal const val CLIPPED_SHADOWS_EXPANDABLE_LIST_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsExpandableListView"
internal const val CLIPPED_SHADOWS_FRAME_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsFrameLayout"
internal const val CLIPPED_SHADOWS_GRID_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsGridView"
internal const val CLIPPED_SHADOWS_LINEAR_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsLinearLayout"
internal const val CLIPPED_SHADOWS_MATERIAL_BUTTON_TOGGLE_GROUP =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsMaterialButtonToggleGroup"
internal const val CLIPPED_SHADOWS_LIST_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsListView"
internal const val CLIPPED_SHADOWS_MOTION_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsMotionLayout"
internal const val CLIPPED_SHADOWS_RADIO_GROUP =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRadioGroup"
internal const val CLIPPED_SHADOWS_RECYCLER_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRecyclerView"
internal const val CLIPPED_SHADOWS_RELATIVE_LAYOUT =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsRelativeLayout"
internal const val CLIPPED_SHADOWS_STACK_VIEW =
    "com.zedalpha.shadowgadgets.view.viewgroup.ClippedShadowsStackView"

internal val ALL_CLIPPED_SHADOWS_VIEW_GROUPS = setOf(
    CLIPPED_SHADOWS_CHIP_GROUP,
    CLIPPED_SHADOWS_CONSTRAINT_LAYOUT,
    CLIPPED_SHADOWS_COORDINATOR_LAYOUT,
    CLIPPED_SHADOWS_EXPANDABLE_LIST_VIEW,
    CLIPPED_SHADOWS_FRAME_LAYOUT,
    CLIPPED_SHADOWS_GRID_VIEW,
    CLIPPED_SHADOWS_LINEAR_LAYOUT,
    CLIPPED_SHADOWS_LIST_VIEW,
    CLIPPED_SHADOWS_MATERIAL_BUTTON_TOGGLE_GROUP,
    CLIPPED_SHADOWS_MOTION_LAYOUT,
    CLIPPED_SHADOWS_RADIO_GROUP,
    CLIPPED_SHADOWS_RECYCLER_VIEW,
    CLIPPED_SHADOWS_RELATIVE_LAYOUT,
    CLIPPED_SHADOWS_STACK_VIEW
)

internal val ALL_LIBRARY_VIEW_GROUPS =
    ALL_SHADOWS_VIEW_GROUPS + ALL_CLIPPED_SHADOWS_VIEW_GROUPS


internal const val ATTR_SHADOW_PLANE =
    "shadowPlane"
internal const val ATTR_CLIP_OUTLINE_SHADOW =
    "clipOutlineShadow"
internal const val ATTR_OUTLINE_SHADOW_COLOR_COMPAT =
    "outlineShadowColorCompat"
internal const val ATTR_FORCE_OUTLINE_SHADOW_COLOR_COMPAT =
    "forceOutlineShadowColorCompat"
internal const val ATTR_CLIPPED_SHADOW_PLANE =
    "clippedShadowPlane"  // Deprecated

internal val ALL_CHILD_SHADOW_ATTRIBUTES = setOf(
    ATTR_SHADOW_PLANE,
    ATTR_CLIP_OUTLINE_SHADOW,
    ATTR_OUTLINE_SHADOW_COLOR_COMPAT,
    ATTR_FORCE_OUTLINE_SHADOW_COLOR_COMPAT,
    ATTR_CLIPPED_SHADOW_PLANE
)

internal const val ATTR_CHILD_SHADOWS_PLANE =
    "childShadowsPlane"
internal const val ATTR_CLIP_ALL_CHILD_SHADOWS =
    "clipAllChildShadows"
internal const val ATTR_CHILD_OUTLINE_SHADOWS_COLOR_COMPAT =
    "childOutlineShadowsColorCompat"
internal const val ATTR_FORCE_CHILD_OUTLINE_SHADOWS_COLOR_COMPAT =
    "forceChildOutlineShadowsColorCompat"
internal const val ATTR_IGNORE_INLINE_CHILD_SHADOWS =
    "ignoreInlineChildShadows"
internal const val ATTR_CHILD_CLIPPED_SHADOWS_PLANE =
    "childClippedShadowsPlane"  // Deprecated

internal val ALL_PARENT_SHADOW_ATTRIBUTES = setOf(
    ATTR_CHILD_SHADOWS_PLANE,
    ATTR_CLIP_ALL_CHILD_SHADOWS,
    ATTR_CHILD_OUTLINE_SHADOWS_COLOR_COMPAT,
    ATTR_FORCE_CHILD_OUTLINE_SHADOWS_COLOR_COMPAT,
    ATTR_IGNORE_INLINE_CHILD_SHADOWS,
    ATTR_CHILD_CLIPPED_SHADOWS_PLANE
)

@Suppress("unused")  // For future use.
internal val ALL_SHADOW_ATTRIBUTES =
    ALL_CHILD_SHADOW_ATTRIBUTES + ALL_PARENT_SHADOW_ATTRIBUTES