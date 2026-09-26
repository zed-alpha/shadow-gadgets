package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.MotionLayoutIdDetector
import com.android.tools.lint.detector.api.Issue
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.copy

class MotionLayoutIdDetectorSG :
    BaseDetector<MotionLayoutIdDetector>(::MotionLayoutIdDetector) {

    companion object {

        @JvmField
        val MISSING_ID_SG: Issue =
            MotionLayoutIdDetector.MISSING_ID
                .copy(MotionLayoutIdDetectorSG::class.java)
    }

    override val issues: Map<Issue, Issue> =
        mapOf(MotionLayoutIdDetector.MISSING_ID to MISSING_ID_SG)

    override val elements: Collection<String> = listOf(SHADOWS_MOTION_LAYOUT)
}