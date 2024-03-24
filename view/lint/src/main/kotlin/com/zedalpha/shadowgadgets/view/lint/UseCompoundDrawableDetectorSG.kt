package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.UseCompoundDrawableDetector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_LINEAR_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_LINEAR_LAYOUT

class UseCompoundDrawableDetectorSG : BaseDetector() {

    companion object {

        @JvmField
        val ISSUE_SG = UseCompoundDrawableDetector.ISSUE.copy(
            Implementation(
                UseCompoundDrawableDetectorSG::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }

    override val detector = UseCompoundDrawableDetector()

    override val issues = mapOf(
        UseCompoundDrawableDetector.ISSUE to ISSUE_SG
    )

    override val elements = listOf(
        SHADOWS_LINEAR_LAYOUT,
        CLIPPED_SHADOWS_LINEAR_LAYOUT
    )
}