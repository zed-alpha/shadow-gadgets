package com.zedalpha.shadowgadgets.move

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

class MoveAlertIssueRegistry : IssueRegistry() {

    override val api: Int get() = CURRENT_API

    override val minApi: Int get() = 12

    override val vendor: Vendor =
        Vendor(
            vendorName = "zed-alpha",
            identifier = "https://github.com/zed-alpha/shadow-gadgets",
            feedbackUrl = "https://github.com/zed-alpha/shadow-gadgets/issues"
        )

    override val issues: List<Issue> = listOf(MoveAlertDetector.ISSUE)
}