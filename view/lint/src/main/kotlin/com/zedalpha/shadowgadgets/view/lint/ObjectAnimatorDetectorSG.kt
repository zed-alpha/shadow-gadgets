package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.checks.ObjectAnimatorDetector
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.LintMap
import com.android.tools.lint.detector.api.Scope
import com.zedalpha.shadowgadgets.view.lint.internal.BaseDetector
import com.zedalpha.shadowgadgets.view.lint.internal.SHADOWS_MOTION_LAYOUT

class ObjectAnimatorDetectorSG : BaseDetector() {

    companion object {

        @JvmField
        val MISSING_KEEP_SG = ObjectAnimatorDetector.MISSING_KEEP.copy(
            Implementation(
                ObjectAnimatorDetectorSG::class.java,
                Scope.RESOURCE_FILE_SCOPE
            )
        )
    }

    override val detector = ObjectAnimatorDetector()

    override val issues = mapOf(
        ObjectAnimatorDetector.MISSING_KEEP to MISSING_KEEP_SG
    )

    override val elements = listOf(SHADOWS_MOTION_LAYOUT, "CustomAttribute")

    override fun filterIncident(
        context: Context,
        incident: Incident,
        map: LintMap
    ): Boolean = detector.filterIncident(context, incident, map)

    override fun getApplicableMethodNames(): List<String> =
        detector.getApplicableMethodNames()
}