package com.zedalpha.shadowgadgets.view.lint

import com.android.SdkConstants
import com.android.tools.lint.checks.InefficientWeightDetector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.CLIPPED_SHADOWS_LINEAR_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_LINEAR_LAYOUT

class InefficientWeightDetectorSG : BaseDetector() {

    companion object {

        private val implementation = Implementation(
            InefficientWeightDetectorSG::class.java,
            Scope.RESOURCE_FILE_SCOPE
        )

        @JvmField
        val BASELINE_WEIGHTS_SG =
            InefficientWeightDetector.BASELINE_WEIGHTS.copy(implementation)

        @JvmField
        val INEFFICIENT_WEIGHT_SG =
            InefficientWeightDetector.INEFFICIENT_WEIGHT.copy(implementation)

        @JvmField
        val NESTED_WEIGHTS_SG =
            InefficientWeightDetector.NESTED_WEIGHTS.copy(implementation)

        @JvmField
        val ORIENTATION_SG =
            InefficientWeightDetector.ORIENTATION.copy(implementation)

        @JvmField
        val WRONG_0DP_SG =
            InefficientWeightDetector.WRONG_0DP.copy(implementation)
    }

    override val detector = InefficientWeightDetector()

    override val issues = mapOf(
        InefficientWeightDetector.BASELINE_WEIGHTS to BASELINE_WEIGHTS_SG,
        InefficientWeightDetector.INEFFICIENT_WEIGHT to INEFFICIENT_WEIGHT_SG,
        InefficientWeightDetector.NESTED_WEIGHTS to NESTED_WEIGHTS_SG,
        InefficientWeightDetector.ORIENTATION to ORIENTATION_SG,
        InefficientWeightDetector.WRONG_0DP to WRONG_0DP_SG
    )

    override val elements = listOf(
        SHADOWS_LINEAR_LAYOUT,
        CLIPPED_SHADOWS_LINEAR_LAYOUT,
        SdkConstants.LINEAR_LAYOUT
    )
}