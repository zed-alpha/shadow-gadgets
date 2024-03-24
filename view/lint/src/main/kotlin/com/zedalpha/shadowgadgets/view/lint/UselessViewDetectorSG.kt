package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.UselessViewDetector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_CHIP_GROUP
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_CONSTRAINT_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_COORDINATOR_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_FRAME_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_LINEAR_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_MATERIAL_BUTTON_TOGGLE_GROUP
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_MOTION_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_RADIO_GROUP
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_RELATIVE_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_CHIP_GROUP
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_CONSTRAINT_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_COORDINATOR_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_FRAME_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_LINEAR_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MATERIAL_BUTTON_TOGGLE_GROUP
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_RADIO_GROUP
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_RELATIVE_LAYOUT

class UselessViewDetectorSG : BaseDetector() {

    companion object {

        private val implementation = Implementation(
            UselessViewDetectorSG::class.java,
            Scope.RESOURCE_FILE_SCOPE
        )

        @JvmField
        val USELESS_PARENT_SG =
            UselessViewDetector.USELESS_PARENT.copy(implementation)

        @JvmField
        val USELESS_LEAF_SG =
            UselessViewDetector.USELESS_LEAF.copy(implementation)
    }

    override val detector = UselessViewDetector()

    override val issues = mapOf(
        UselessViewDetector.USELESS_PARENT to USELESS_PARENT_SG,
        UselessViewDetector.USELESS_LEAF to USELESS_LEAF_SG
    )

    override val elements = listOf(
        SHADOWS_CHIP_GROUP,
        CLIPPED_SHADOWS_CHIP_GROUP,
        SHADOWS_CONSTRAINT_LAYOUT,
        CLIPPED_SHADOWS_CONSTRAINT_LAYOUT,
        SHADOWS_COORDINATOR_LAYOUT,
        CLIPPED_SHADOWS_COORDINATOR_LAYOUT,
        SHADOWS_FRAME_LAYOUT,
        CLIPPED_SHADOWS_FRAME_LAYOUT,
        SHADOWS_LINEAR_LAYOUT,
        CLIPPED_SHADOWS_LINEAR_LAYOUT,
        SHADOWS_MATERIAL_BUTTON_TOGGLE_GROUP,
        CLIPPED_SHADOWS_MATERIAL_BUTTON_TOGGLE_GROUP,
        SHADOWS_MOTION_LAYOUT,
        CLIPPED_SHADOWS_MOTION_LAYOUT,
        SHADOWS_RADIO_GROUP,
        CLIPPED_SHADOWS_RADIO_GROUP,
        SHADOWS_RELATIVE_LAYOUT,
        CLIPPED_SHADOWS_RELATIVE_LAYOUT,
        // Omitted, as in the native Detector.
        // SHADOWS_EXPANDABLE_LIST_VIEW,
        // CLIPPED_SHADOWS_EXPANDABLE_LIST_VIEW,
        // SHADOWS_GRID_VIEW,
        // CLIPPED_SHADOWS_GRID_VIEW,
        // SHADOWS_LIST_VIEW,
        // CLIPPED_SHADOWS_LIST_VIEW,
        // SHADOWS_RECYCLER_VIEW,
        // CLIPPED_SHADOWS_RECYCLER_VIEW,
        // SHADOWS_STACK_VIEW,
        // CLIPPED_SHADOWS_STACK_VIEW
    )
}