package com.zedalpha.shadowgadgets.view.lint.internal

import com.android.resources.ResourceFolderType
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Project
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.TextFormat
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.XmlScanner
import org.w3c.dom.Element
import java.lang.reflect.Field
import java.util.EnumSet

abstract class BaseDetector<T : Detector>(createDetector: () -> T) :
    Detector(), XmlScanner {

    protected val detector: T = createDetector()

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

    override fun visitElement(context: XmlContext, element: Element) =
        detector.visitElement(xmlContextWrapper, element)
}

internal fun Issue.copy(
    detectorClass: Class<out Detector>,
    scope: EnumSet<Scope>? = null
): Issue =
    Issue.create(
        id = "${this.id}SG",
        briefDescription = getBriefDescription(TextFormat.RAW),
        explanation = getExplanation(TextFormat.RAW),
        category = this.category,
        priority = this.priority,
        severity = this.defaultSeverity,
        implementation =
            Implementation(
                /* detectorClass = */ detectorClass,
                /* scope = */ scope ?: this.implementation.scope,
                /* ...analysisScopes = */ *this.implementation.analysisScopes
            )
    )

private fun Context.wrapProjectConfiguration(issues: Map<Issue, Issue>) {
    val wrapper = ConfigurationWrapper(project.getConfiguration(driver), issues)
    try {
        ProjectConfigurationField?.set(project, wrapper)
    } catch (_: Throwable) {
        /* ignore */
    }
}

private val ProjectConfigurationField: Field? =
    try {
        Project::class.java
            .getDeclaredField("configuration")
            .apply { isAccessible = true }
    } catch (_: Throwable) {
        null
    }