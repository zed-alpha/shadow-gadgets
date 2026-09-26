package com.zedalpha.shadowgadgets.view.lint

import com.android.tools.lint.client.api.LintTomlDocument
import com.android.tools.lint.client.api.LintTomlMapValue
import com.android.tools.lint.client.api.TomlContext
import com.android.tools.lint.client.api.TomlScanner
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Detector.GradleScanner
import com.android.tools.lint.detector.api.GradleContext
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity

class JitPackGroupDetector : Detector(), GradleScanner, TomlScanner {

    companion object {

        private const val SG_JITPACK_GROUP =
            "com.github.zed-alpha.shadow-gadgets"

        private const val MESSAGE =
            "Shadow Gadgets has moved to Maven Central: " +
                    "https://github.com/zed-alpha/shadow-gadgets#download"

        @JvmField
        val ISSUE: Issue =
            Issue.create(
                id = "ShadowGadgetsMoved",
                briefDescription = MESSAGE,
                explanation = MESSAGE,
                category = Category.CORRECTNESS,
                priority = 5,
                severity = Severity.INFORMATIONAL,
                implementation =
                    Implementation(
                        /* detectorClass = */
                        JitPackGroupDetector::class.java,
                        /* scope = */
                        Scope.GRADLE_AND_TOML_SCOPE,
                        /* ...analysisScopes = */
                        Scope.GRADLE_SCOPE,
                        Scope.TOML_SCOPE
                    )
            )

        private fun report(context: Context, location: Location) =
            context.report(ISSUE, location, MESSAGE)
    }

    override fun checkDslPropertyAssignment(
        context: GradleContext,
        property: String,
        value: String,
        parent: String,
        parentParent: String?,
        propertyCookie: Any,
        valueCookie: Any,
        statementCookie: Any
    ) {
        if (parent == "dependencies" && value.contains(SG_JITPACK_GROUP)) {
            report(context, context.getLocation(valueCookie))
        }
    }

    override fun visitTomlDocument(
        context: TomlContext,
        document: LintTomlDocument
    ) {
        val libraries =
            document.getValue("libraries") as? LintTomlMapValue ?: return

        libraries.getMappedValues().values.forEach { value ->
            if (value.getText().contains(SG_JITPACK_GROUP)) {
                report(context, context.getLocation(value))
            }
        }
    }
}