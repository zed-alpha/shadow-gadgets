package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.RelativeOverlapDetector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_RELATIVE_LAYOUT

class RelativeOverlapDetectorSG : BaseDetector() {

    companion object {

        @JvmField
        val ISSUE_SG = RelativeOverlapDetector.ISSUE.copy(
            Implementation(
                RelativeOverlapDetectorSG::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }

    override val detector = RelativeOverlapDetector()

    override val issues = mapOf(
        RelativeOverlapDetector.ISSUE to ISSUE_SG
    )

    override val elements = listOf(SHADOWS_RELATIVE_LAYOUT)
}