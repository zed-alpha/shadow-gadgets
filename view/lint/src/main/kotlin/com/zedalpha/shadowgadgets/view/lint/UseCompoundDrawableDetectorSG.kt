package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.UseCompoundDrawableDetector
import com.android.tools.lint.detector.api.Issue
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_LINEAR_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.copy

class UseCompoundDrawableDetectorSG :
    BaseDetector<UseCompoundDrawableDetector>(::UseCompoundDrawableDetector) {

    companion object {

        @JvmField
        val ISSUE_SG: Issue =
            UseCompoundDrawableDetector.ISSUE
                .copy(UseCompoundDrawableDetectorSG::class.java)
    }

    override val issues: Map<Issue, Issue> =
        mapOf(UseCompoundDrawableDetector.ISSUE to ISSUE_SG)

    override val elements: Collection<String> = listOf(SHADOWS_LINEAR_LAYOUT)
}