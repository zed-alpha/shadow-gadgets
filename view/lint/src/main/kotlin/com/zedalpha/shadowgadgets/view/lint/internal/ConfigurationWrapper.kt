package com.zedalpha.shadowgadgets.view.lint.internal

import com.android.tools.lint.client.api.Configuration
import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Severity
import java.io.File

internal class ConfigurationWrapper(
    private val wrapped: Configuration,
    private val issueMap: Map<Issue, Issue>,
) : Configuration(wrapped.configurations) {

    init {
        wrapped.parent?.let { setParent(it) }
    }

    override var baselineFile: File? by wrapped::baselineFile

    override fun isEnabled(issue: Issue): Boolean =
        issueMap[issue]?.let { super.isEnabled(it) } ?: false

    override fun addConfiguredIssues(
        targetMap: MutableMap<String, Severity>,
        registry: IssueRegistry,
        specificOnly: Boolean,
    ) {
        wrapped.addConfiguredIssues(targetMap, registry, specificOnly)
    }

    override fun ignore(
        context: Context,
        issue: Issue,
        location: Location?,
        message: String,
    ) {
        wrapped.ignore(context, issueMap[issue] ?: issue, location, message)
    }

    override fun ignore(issue: Issue, file: File) {
        wrapped.ignore(issueMap[issue] ?: issue, file)
    }

    override fun ignore(issueId: String, file: File) {
        val issue = issueMap.entries.firstOrNull { it.key.id == issueId }?.value
        wrapped.ignore(issue?.id ?: issueId, file)
    }

    override fun setSeverity(issue: Issue, severity: Severity?) {
        wrapped.setSeverity(issueMap[issue] ?: issue, severity)
    }

    override fun getDefaultSeverity(
        issue: Issue,
        visibleDefault: Severity
    ): Severity = super.getDefaultSeverity(
        issueMap[issue] ?: issue,
        visibleDefault
    )

    override fun getDefinedSeverity(
        issue: Issue,
        source: Configuration,
        visibleDefault: Severity
    ): Severity? = super.getDefinedSeverity(
        issueMap[issue] ?: issue,
        source,
        visibleDefault
    )

    override fun getOption(
        issue: Issue,
        name: String,
        default: String?
    ): String? = super.getOption(issueMap[issue] ?: issue, name, default)

    override fun getOptionAsFile(
        issue: Issue,
        name: String,
        default: File?
    ): File? = super.getOptionAsFile(issueMap[issue] ?: issue, name, default)
}