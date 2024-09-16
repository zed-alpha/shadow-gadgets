package com.zedalpha.shadowgadgets.view.lint.internal

import com.android.ide.common.resources.ResourceItem
import com.android.ide.common.resources.ResourceRepository
import com.android.ide.common.util.PathString
import com.android.sdklib.IAndroidTarget
import com.android.tools.lint.client.api.Configuration
import com.android.tools.lint.client.api.ConfigurationHierarchy
import com.android.tools.lint.client.api.GradleVisitor
import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.LintClient
import com.android.tools.lint.client.api.LintDriver
import com.android.tools.lint.client.api.PlatformLookup
import com.android.tools.lint.client.api.ResourceRepositoryScope
import com.android.tools.lint.client.api.SdkInfo
import com.android.tools.lint.client.api.UastParser
import com.android.tools.lint.client.api.XmlParser
import com.android.tools.lint.detector.api.Constraint
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Desugaring
import com.android.tools.lint.detector.api.Incident
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LintMap
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.PartialResult
import com.android.tools.lint.detector.api.Project
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.TextFormat
import com.android.tools.lint.model.PathVariables
import com.android.utils.Pair
import com.intellij.openapi.util.Computable
import com.intellij.pom.java.LanguageLevel
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.xmlpull.v1.XmlPullParser
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLConnection

class ClientWrapper(
    private val delegate: LintClient,
    private val issueMap: Map<Issue, Issue>
) : LintClient(clientName) {

    override val configurations: ConfigurationHierarchy =
        delegate.configurations

    override val printInternalErrorStackTrace: Boolean
        get() = delegate.printInternalErrorStackTrace

    override fun getConfiguration(
        project: Project,
        driver: LintDriver?
    ): Configuration = ConfigurationWrapper(
        delegate.getConfiguration(project, driver),
        issueMap
    )

    override fun getConfiguration(file: File): Configuration? =
        delegate.getConfiguration(file)
            ?.let { ConfigurationWrapper(it, issueMap) }

    override fun report(
        context: Context,
        incident: Incident,
        format: TextFormat
    ) {
        issueMap[incident.issue]?.let { incident.issue = it }
        delegate.report(context, incident, format)
    }

    override fun report(
        context: Context,
        incident: Incident,
        constraint: Constraint
    ) {
        issueMap[incident.issue]?.let { incident.issue = it }
        delegate.report(context, incident, constraint)
    }

    override fun report(context: Context, incident: Incident, map: LintMap) {
        issueMap[incident.issue]?.let { incident.issue = it }
        delegate.report(context, incident, map)
    }

    override fun getPartialResults(
        project: Project,
        issue: Issue
    ): PartialResult =
        delegate.getPartialResults(project, issueMap[issue] ?: issue)

    override fun getMergedManifest(project: Project): Document? =
        delegate.getMergedManifest(project)

    override fun resolveMergeManifestSources(
        mergedManifest: Document,
        reportFile: Any
    ) {
        delegate.resolveMergeManifestSources(mergedManifest, reportFile)
    }

    override fun findManifestSourceNode(mergedNode: Node): Pair<File, out Node>? =
        delegate.findManifestSourceNode(mergedNode)

    override fun findManifestSourceLocation(mergedNode: Node): Location? =
        delegate.findManifestSourceLocation(mergedNode)

    override fun getXmlDocument(
        file: File,
        contents: CharSequence?
    ): Document? = delegate.getXmlDocument(file, contents)

    override fun getClientDisplayName(): String {
        return delegate.getClientDisplayName()
    }

    override fun getDisplayPath(
        file: File,
        project: Project?,
        format: TextFormat
    ): String =
        delegate.getDisplayPath(file, project, format)

    override fun log(
        severity: Severity,
        exception: Throwable?,
        format: String?,
        vararg args: Any
    ) = delegate.log(severity, exception, format, *args)

    override fun getTestLibraries(project: Project): List<File> =
        delegate.getTestLibraries(project)

    override fun getClientRevision(): String? = delegate.getClientRevision()

    override fun getClientDisplayRevision(): String? =
        delegate.getClientDisplayRevision()

    override fun runReadAction(runnable: Runnable) =
        delegate.runReadAction(runnable)

    override fun <T> runReadAction(computable: Computable<T>): T =
        delegate.runReadAction(computable)

    override fun readFile(file: File): CharSequence = delegate.readFile(file)

    @Throws(IOException::class)
    override fun readBytes(file: File): ByteArray = delegate.readBytes(file)

    override fun getJavaSourceFolders(project: Project): List<File> =
        delegate.getJavaSourceFolders(project)

    override fun getGeneratedSourceFolders(project: Project): List<File> =
        delegate.getGeneratedSourceFolders(project)

    override fun getJavaClassFolders(project: Project): List<File> =
        delegate.getJavaClassFolders(project)

    override fun getJavaLibraries(
        project: Project,
        includeProvided: Boolean
    ): List<File> = delegate.getJavaLibraries(project, includeProvided)

    override fun getTestSourceFolders(project: Project): List<File> =
        delegate.getTestSourceFolders(project)

    override fun createSuperClassMap(project: Project): Map<String, String> =
        delegate.createSuperClassMap(project)

    override fun getResourceFolders(project: Project): List<File> =
        delegate.getResourceFolders(project)

    override val xmlParser: XmlParser get() = delegate.xmlParser

    override fun getSdkInfo(project: Project): SdkInfo =
        delegate.getSdkInfo(project)

    override fun getProject(dir: File, referenceDir: File): Project =
        delegate.getProject(dir, referenceDir)

    override fun getUastParser(project: Project?): UastParser =
        delegate.getUastParser(project)

    override fun findResource(relativePath: String): File? =
        delegate.findResource(relativePath)

    override fun getCacheDir(name: String?, create: Boolean): File? =
        delegate.getCacheDir(name, create)

    override fun log(exception: Throwable?, format: String?, vararg args: Any) =
        delegate.log(exception, format, *args)

    override fun initializeProjects(
        driver: LintDriver?,
        knownProjects: Collection<Project>
    ): Unit = throw UnsupportedOperationException()

    override fun disposeProjects(knownProjects: Collection<Project>): Unit =
        throw UnsupportedOperationException()

    override fun getSdkHome(): File? = delegate.getSdkHome()

    override fun getTargets(): List<IAndroidTarget> = delegate.getTargets()

    override fun getCompileTarget(project: Project): IAndroidTarget? =
        delegate.getCompileTarget(project)

    override fun getSuperClass(project: Project, name: String): String? =
        delegate.getSuperClass(project, name)

    override fun isSubclassOf(
        project: Project,
        name: String,
        superClassName: String
    ): Boolean? = delegate.isSubclassOf(project, name, superClassName)

    override fun getProjectName(project: Project): String =
        delegate.getProjectName(project)

    override fun isGradleProject(project: Project): Boolean =
        delegate.isGradleProject(project)

    override fun createProject(dir: File, referenceDir: File): Project =
        throw UnsupportedOperationException()

    override fun findGlobalRuleJars(
        driver: LintDriver?,
        warnDeprecated: Boolean
    ): List<File> = delegate.findGlobalRuleJars(driver, warnDeprecated)

    override fun findRuleJars(project: Project): Iterable<File> =
        delegate.findRuleJars(project)

    override fun isProjectDirectory(dir: File): Boolean =
        delegate.isProjectDirectory(dir)

    override fun registerProject(dir: File, project: Project): Unit =
        throw UnsupportedOperationException()

    override fun addCustomLintRules(
        registry: IssueRegistry,
        driver: LintDriver?,
        warnDeprecated: Boolean
    ): IssueRegistry =
        delegate.addCustomLintRules(registry, driver, warnDeprecated)

    override fun getAssetFolders(project: Project): List<File> =
        delegate.getAssetFolders(project)

    @Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")
    override fun createUrlClassLoader(
        urls: Array<URL>,
        parent: ClassLoader
    ): ClassLoader = delegate.createUrlClassLoader(urls, parent)

    override fun createUrlClassLoader(
        files: List<File>,
        parent: ClassLoader
    ): ClassLoader = delegate.createUrlClassLoader(files, parent)

    override fun checkForSuppressComments(): Boolean =
        delegate.checkForSuppressComments()

    override fun getResources(
        project: Project,
        scope: ResourceRepositoryScope
    ): ResourceRepository = delegate.getResources(project, scope)

    override fun createResourceItemHandle(
        item: ResourceItem,
        nameOnly: Boolean,
        valueOnly: Boolean
    ): Location.ResourceItemHandle =
        delegate.createResourceItemHandle(item, nameOnly, valueOnly)

    override fun getLatestSdkTarget(
        minApi: Int,
        includePreviews: Boolean
    ): IAndroidTarget? = delegate.getLatestSdkTarget(minApi, includePreviews)

    override fun getPlatformLookup(): PlatformLookup? =
        delegate.getPlatformLookup()

    @Throws(IOException::class)
    override fun openConnection(url: URL): URLConnection? =
        delegate.openConnection(url)

    @Throws(IOException::class)
    override fun openConnection(url: URL, timeout: Int): URLConnection? =
        delegate.openConnection(url, timeout)

    override fun closeConnection(connection: URLConnection) =
        delegate.closeConnection(connection)

    override fun getGradleVisitor(): GradleVisitor = delegate.getGradleVisitor()

    override fun getGeneratedResourceFolders(project: Project): List<File> =
        delegate.getGeneratedResourceFolders(project)

    override fun readBytes(resourcePath: PathString): ByteArray =
        delegate.readBytes(resourcePath)

    override fun getDesugaring(project: Project): Set<Desugaring> =
        delegate.getDesugaring(project)

    override fun createXmlPullParser(resourcePath: PathString): XmlPullParser? =
        delegate.createXmlPullParser(resourcePath)

    override fun getExternalAnnotations(projects: Collection<Project>): List<File> =
        delegate.getExternalAnnotations(projects)

    override fun getRelativePath(baseFile: File?, file: File?): String? =
        delegate.getRelativePath(baseFile, file)

    override fun getJdkHome(project: Project?): File? =
        delegate.getJdkHome(project)

    override fun getJavaLanguageLevel(project: Project): LanguageLevel =
        delegate.getJavaLanguageLevel(project)

    override fun getKotlinLanguageLevel(project: Project): LanguageVersionSettings =
        delegate.getKotlinLanguageLevel(project)

    override fun supportsPartialAnalysis(): Boolean =
        delegate.supportsPartialAnalysis()

    override fun storeState(project: Project) {
        delegate.storeState(project)
    }

    override fun mergeState(roots: Collection<Project>, driver: LintDriver) {
        delegate.mergeState(roots, driver)
    }

    override fun getRootDir(): File? = delegate.getRootDir()

    override val pathVariables: PathVariables
        get() = delegate.pathVariables

    override fun isEdited(
        file: File,
        returnIfUnknown: Boolean,
        savedSinceMsAgo: Long
    ): Boolean = delegate.isEdited(file, returnIfUnknown, savedSinceMsAgo)

    override fun fileExists(
        file: File,
        requireFile: Boolean,
        requireDirectory: Boolean
    ): Boolean = delegate.fileExists(file, requireFile, requireDirectory)
}