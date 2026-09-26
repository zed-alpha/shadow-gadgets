package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.ChildCountDetector
import com.android.tools.lint.detector.api.Issue
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_GRID_VIEW
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_LIST_VIEW
import com.zedalpha.shadowgadgets.view.lint.internal.copy

class ChildCountDetectorSG :
    BaseDetector<ChildCountDetector>(::ChildCountDetector) {

    companion object {

        @JvmField
        val ADAPTER_VIEW_ISSUE_SG: Issue =
            ChildCountDetector.ADAPTER_VIEW_ISSUE
                .copy(ChildCountDetectorSG::class.java)
    }

    override val issues: Map<Issue, Issue> =
        mapOf(ChildCountDetector.ADAPTER_VIEW_ISSUE to ADAPTER_VIEW_ISSUE_SG)

    override val elements: Collection<String> =
        listOf(SHADOWS_GRID_VIEW, SHADOWS_LIST_VIEW)
}