package com.zedalpha.shadowgadgets.view.lint

import com.android.SdkConstants.MotionSceneTags.MOTION_SCENE
import com.android.tools.lint.checks.MotionLayoutDetector
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT

class MotionLayoutDetectorSG : BaseDetector() {

    companion object {

        @JvmField
        val INVALID_SCENE_FILE_REFERENCE_SG =
            MotionLayoutDetector.INVALID_SCENE_FILE_REFERENCE.copy(
                Implementation(
                    MotionLayoutDetectorSG::class.java,
                    Scope.RESOURCE_FILE_SCOPE
                )
            )
    }

    override val detector = MotionLayoutDetector()

    override val issues = mapOf(
        MotionLayoutDetector.INVALID_SCENE_FILE_REFERENCE to
                INVALID_SCENE_FILE_REFERENCE_SG
    )

    override val elements = listOf(SHADOWS_MOTION_LAYOUT, MOTION_SCENE)

    override fun afterCheckRootProject(context: Context) {
        detector.afterCheckRootProject(contextWrapper)
    }
}