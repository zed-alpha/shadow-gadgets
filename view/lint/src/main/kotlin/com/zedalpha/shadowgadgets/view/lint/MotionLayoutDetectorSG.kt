package com.zedalpha.shadowgadgets.view.lint

import com.android.SdkConstants.MotionSceneTags.MOTION_SCENE
import com.android.tools.lint.checks.MotionLayoutDetector
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Issue
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.copy

class MotionLayoutDetectorSG :
    BaseDetector<MotionLayoutDetector>(::MotionLayoutDetector) {

    companion object {

        @JvmField
        val INVALID_SCENE_FILE_REFERENCE_SG: Issue =
            MotionLayoutDetector.INVALID_SCENE_FILE_REFERENCE
                .copy(MotionLayoutDetectorSG::class.java)
    }

    override val issues: Map<Issue, Issue> =
        mapOf(
            MotionLayoutDetector.INVALID_SCENE_FILE_REFERENCE to
                    INVALID_SCENE_FILE_REFERENCE_SG
        )

    override val elements: Collection<String> =
        listOf(SHADOWS_MOTION_LAYOUT, MOTION_SCENE)

    override fun afterCheckRootProject(context: Context) =
        detector.afterCheckRootProject(contextWrapper)
}