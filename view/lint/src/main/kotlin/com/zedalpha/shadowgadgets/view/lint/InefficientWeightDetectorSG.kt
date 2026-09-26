package com.zedalpha.shadowgadgets.view.lint

import com.android.SdkConstants
import com.android.tools.lint.checks.InefficientWeightDetector
import com.android.tools.lint.detector.api.Issue
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_LINEAR_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.copy

class InefficientWeightDetectorSG :
    BaseDetector<InefficientWeightDetector>(::InefficientWeightDetector) {

    companion object {

        @JvmField
        val BASELINE_WEIGHTS_SG: Issue =
            InefficientWeightDetector.BASELINE_WEIGHTS
                .copy(InefficientWeightDetectorSG::class.java)

        @JvmField
        val INEFFICIENT_WEIGHT_SG: Issue =
            InefficientWeightDetector.INEFFICIENT_WEIGHT
                .copy(InefficientWeightDetectorSG::class.java)

        @JvmField
        val NESTED_WEIGHTS_SG: Issue =
            InefficientWeightDetector.NESTED_WEIGHTS
                .copy(InefficientWeightDetectorSG::class.java)

        @JvmField
        val ORIENTATION_SG: Issue =
            InefficientWeightDetector.ORIENTATION
                .copy(InefficientWeightDetectorSG::class.java)

        @JvmField
        val WRONG_0DP_SG: Issue =
            InefficientWeightDetector.WRONG_0DP
                .copy(InefficientWeightDetectorSG::class.java)
    }

    override val issues: Map<Issue, Issue> =
        mapOf(
            InefficientWeightDetector.BASELINE_WEIGHTS to BASELINE_WEIGHTS_SG,
            InefficientWeightDetector.INEFFICIENT_WEIGHT to INEFFICIENT_WEIGHT_SG,
            InefficientWeightDetector.NESTED_WEIGHTS to NESTED_WEIGHTS_SG,
            InefficientWeightDetector.ORIENTATION to ORIENTATION_SG,
            InefficientWeightDetector.WRONG_0DP to WRONG_0DP_SG
        )

    override val elements: Collection<String> =
        listOf(SHADOWS_LINEAR_LAYOUT, SdkConstants.LINEAR_LAYOUT)
}