package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.RelativeOverlapDetector
import com.android.tools.lint.detector.api.Issue
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_RELATIVE_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.copy

class RelativeOverlapDetectorSG :
    BaseDetector<RelativeOverlapDetector>(::RelativeOverlapDetector) {

    companion object {

        @JvmField
        val ISSUE_SG: Issue =
            RelativeOverlapDetector.ISSUE
                .copy(RelativeOverlapDetectorSG::class.java)
    }

    override val issues: Map<Issue, Issue> =
        mapOf(RelativeOverlapDetector.ISSUE to ISSUE_SG)

    override val elements: Collection<String> = listOf(SHADOWS_RELATIVE_LAYOUT)
}