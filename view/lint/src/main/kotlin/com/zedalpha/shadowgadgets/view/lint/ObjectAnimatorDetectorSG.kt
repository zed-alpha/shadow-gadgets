package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.ObjectAnimatorDetector
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintMap
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT
import com.zedalpha.shadowgadgets.view.lint.internal.copy

class ObjectAnimatorDetectorSG :
    BaseDetector<ObjectAnimatorDetector>(::ObjectAnimatorDetector) {

    companion object {

        // Need to adjust the scope since we dropped SourceCodeScanner.
        @JvmField
        val MISSING_KEEP_SG: Issue =
            ObjectAnimatorDetector.MISSING_KEEP.copy(
                detectorClass = ObjectAnimatorDetectorSG::class.java,
                scope = Scope.RESOURCE_FILE_SCOPE
            )
    }

    override val issues: Map<Issue, Issue> =
        mapOf(ObjectAnimatorDetector.MISSING_KEEP to MISSING_KEEP_SG)

    override val elements: Collection<String> =
        listOf(SHADOWS_MOTION_LAYOUT, "CustomAttribute")

    override fun filterIncident(
        context: Context,
        incident: Incident,
        map: LintMap
    ): Boolean =
        detector.filterIncident(context, incident, map)

    override fun getApplicableMethodNames(): List<String> =
        detector.getApplicableMethodNames()
}