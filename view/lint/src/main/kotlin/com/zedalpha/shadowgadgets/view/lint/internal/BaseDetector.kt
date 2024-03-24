package com.zedalpha.shadowgadgets.view.lint.internal

import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Project
import com.android.tools.lint.detector.api.TextFormat
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import org.w3c.dom.Element

abstract class BaseDetector : Detector(), XmlScanner {

    abstract val detector: Detector

    abstract val issues: Map<Issue, Issue>

    override fun appliesTo(folderType: ResourceFolderType) =
        detector.appliesTo(folderType)

    abstract val elements: Collection<String>

    override fun getApplicableElements() = elements

    protected lateinit var contextWrapper: ContextWrapper

    final override fun beforeCheckRootProject(context: Context) {
        context.wrapProjectConfiguration(issues)
        contextWrapper = ContextWrapper(context, issues)
    }

    protected lateinit var xmlContextWrapper: XmlContextWrapper

    override fun beforeCheckFile(context: Context) {
        xmlContextWrapper = XmlContextWrapper(context as XmlContext, issues)
    }

    override fun visitElement(context: XmlContext, element: Element) {
        detector.visitElement(xmlContextWrapper, element)
    }

    companion object {

        internal fun Issue.copy(
            implementation: Implementation
        ) = Issue.create(
            id = "${id}SG",
            briefDescription = getBriefDescription(TextFormat.RAW),
            explanation = getExplanation(TextFormat.RAW),
            category = category,
            priority = priority,
            severity = defaultSeverity,
            implementation = implementation
        )
    }
}

private fun Context.wrapProjectConfiguration(issues: Map<Issue, Issue>) {
    val wrapper = ConfigurationWrapper(project.getConfiguration(driver), issues)
    try {
        ProjectConfigurationField?.set(project, wrapper)
    } catch (e: Throwable) {
        /* ignore */
    }
}

private val ProjectConfigurationField = try {
    Project::class.java
        .getDeclaredField("configuration")
        .apply { isAccessible = true }
} catch (e: Throwable) {
    null
}