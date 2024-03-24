package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class ViewIssueRegistry : IssueRegistry() {

    override val api: Int get() = CURRENT_API

    override val minApi: Int get() = 12

    override val issues: List<Issue> = allIssues

    override val vendor: Vendor = Vendor(
        vendorName = "zed-alpha",
        feedbackUrl = "https://github.com/zed-alpha/shadow-gadgets/issues"
    )

    companion object {

        private val allIssues = listOf(
            ChildCountDetectorSG.ADAPTER_VIEW_ISSUE_SG,
            ConstraintLayoutDetectorSG.ISSUE_SG,
            InefficientWeightDetectorSG.BASELINE_WEIGHTS_SG,
            InefficientWeightDetectorSG.INEFFICIENT_WEIGHT_SG,
            InefficientWeightDetectorSG.NESTED_WEIGHTS_SG,
            InefficientWeightDetectorSG.ORIENTATION_SG,
            InefficientWeightDetectorSG.WRONG_0DP_SG,
            MotionLayoutDetectorSG.INVALID_SCENE_FILE_REFERENCE_SG,
            MotionLayoutIdDetectorSG.MISSING_ID_SG,
            NestedScrollingWidgetDetectorSG.ISSUE_SG,
            ObjectAnimatorDetectorSG.MISSING_KEEP_SG,
            RelativeOverlapDetectorSG.ISSUE_SG,
            UseCompoundDrawableDetectorSG.ISSUE_SG,
            UselessViewDetectorSG.USELESS_LEAF_SG,
            UselessViewDetectorSG.USELESS_PARENT_SG,
            WrongIdDetectorSG.UNKNOWN_ID_SG,
            WrongIdDetectorSG.NOT_SIBLING_SG,
            WrongIdDetectorSG.INVALID_SG,
            WrongIdDetectorSG.UNKNOWN_ID_LAYOUT_SG,
            ShadowAttributesDetector.MISSING_ID
        )
    }
}