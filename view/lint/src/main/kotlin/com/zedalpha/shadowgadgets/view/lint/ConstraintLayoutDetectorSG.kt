package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.ConstraintLayoutDetector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_CONSTRAINT_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT

class ConstraintLayoutDetectorSG : BaseDetector() {

    companion object {

        @JvmField
        val ISSUE_SG = ConstraintLayoutDetector.ISSUE.copy(
            Implementation(
                ConstraintLayoutDetectorSG::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }

    override val detector = ConstraintLayoutDetector()

    override val issues = mapOf(
        ConstraintLayoutDetector.ISSUE to ISSUE_SG
    )

    override val elements = listOf(
        SHADOWS_CONSTRAINT_LAYOUT,
        SHADOWS_MOTION_LAYOUT
    )
}