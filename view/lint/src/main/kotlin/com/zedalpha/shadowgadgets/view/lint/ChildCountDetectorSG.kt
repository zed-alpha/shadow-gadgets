package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.ChildCountDetector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_GRID_VIEW
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_LIST_VIEW

class ChildCountDetectorSG : BaseDetector() {

    companion object {

        @JvmField
        val ADAPTER_VIEW_ISSUE_SG = ChildCountDetector.ADAPTER_VIEW_ISSUE.copy(
            Implementation(
                ChildCountDetectorSG::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }

    override val detector = ChildCountDetector()

    override val issues = mapOf(
        ChildCountDetector.ADAPTER_VIEW_ISSUE to ADAPTER_VIEW_ISSUE_SG
    )

    override val elements = listOf(SHADOWS_GRID_VIEW, SHADOWS_LIST_VIEW)
}