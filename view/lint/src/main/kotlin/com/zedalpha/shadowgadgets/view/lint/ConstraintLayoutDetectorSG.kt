package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.ConstraintLayoutDetector
import com.android.tools.lint.detector.api.Issue
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_CONSTRAINT_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.copy

class ConstraintLayoutDetectorSG :
    BaseDetector<ConstraintLayoutDetector>(::ConstraintLayoutDetector) {

    companion object {

        @JvmField
        val ISSUE_SG: Issue =
            ConstraintLayoutDetector.ISSUE
                .copy(ConstraintLayoutDetectorSG::class.java)
    }

    override val issues: Map<Issue, Issue> =
        mapOf(ConstraintLayoutDetector.ISSUE to ISSUE_SG)

    override val elements: Collection<String> =
        listOf(SHADOWS_CONSTRAINT_LAYOUT, SHADOWS_MOTION_LAYOUT)
}