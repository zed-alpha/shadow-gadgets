package com.zedalpha.shadowgadgets.view.lint.internal

import com.android.resources.ResourceFolderType
import com.android.tools.lint.client.api.LintDriver
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.Project
import com.android.tools.lint.detector.api.XmlContext
import org.w3c.dom.Document
import java.io.File
import java.lang.reflect.Field

class ContextWrapper
private constructor(
    driver: LintDriver,
    project: Project,
    file: File,
    contents: CharSequence?,
) : Context(driver, project, null, file, contents) {

    constructor(wrapped: Context, issues: Map<Issue, Issue>) :
            this(
                driver = createDriver(wrapped, issues),
                project = wrapped.project,
                file = wrapped.file,
                contents = wrapped.getContents(),
            )
}

class XmlContextWrapper
private constructor(
    driver: LintDriver,
    project: Project,
    file: File,
    folderType: ResourceFolderType?,
    contents: CharSequence?,
    document: Document,
) : XmlContext(driver, project, null, file, folderType, contents, document) {

    constructor(wrapped: XmlContext, issues: Map<Issue, Issue>) :
            this(
                driver = createDriver(wrapped, issues),
                project = wrapped.project,
                file = wrapped.file,
                folderType = wrapped.resourceFolderType,
                contents = wrapped.getContents(),
                document = wrapped.document
            )
}

private fun createDriver(
    wrappedContext: Context,
    issues: Map<Issue, Issue>
): LintDriver {
    val clientWrapper = ClientWrapper(wrappedContext.client, issues)
    val wrapped = wrappedContext.driver
    val driver = LintDriver(wrapped.registry, clientWrapper, wrapped.request)
    try {
        LintDriverClientField?.set(driver, clientWrapper)
    } catch (_: Throwable) {
        /* ignore */
    }
    return driver
}

private val LintDriverClientField: Field? =
    try {
        LintDriver::class.java
            .getDeclaredField("client")
            .apply { isAccessible = true }
    } catch (_: Throwable) {
        null
    }