package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.MotionLayoutIdDetector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT

class MotionLayoutIdDetectorSG : BaseDetector() {

    companion object {

        @JvmField
        val MISSING_ID_SG = MotionLayoutIdDetector.MISSING_ID.copy(
            Implementation(
                MotionLayoutIdDetectorSG::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }

    override val detector = MotionLayoutIdDetector()

    override val issues = mapOf(
        MotionLayoutIdDetector.MISSING_ID to MISSING_ID_SG
    )

    override val elements = listOf(SHADOWS_MOTION_LAYOUT)
}