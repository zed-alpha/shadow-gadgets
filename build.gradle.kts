import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import org.jetbrains.dokka.versioning.VersioningConfiguration
import org.jetbrains.dokka.versioning.VersioningPlugin
import java.time.Year

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.compose.compiler) apply false
    alias(libs.plugins.dokka)
}

buildscript {
    dependencies {
        classpath(libs.dokka.gradle.plugin)
        classpath(libs.dokka.versioning.plugin)
    }
}

dependencies {
    dokkaHtmlMultiModulePlugin(libs.dokka.versioning.plugin)
}

// Run with ./gradlew :dokkaHtmlMultiModule. Otherwise :view:lint breaks it.
// Moving files between here and the docs branch is handled manually for now.
tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(rootDir.resolve("docs"))
    pluginConfiguration<VersioningPlugin, VersioningConfiguration> {
        olderVersionsDir = rootDir.resolve("previous")
        version = rootProject.version.toString()
    }
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("images/logo-icon.svg"))
        homepageLink = repositoryUrl
        footerMessage = copyright()
    }
}

subprojects {
    if (name != "compose" && name != "view") return@subprojects

    apply(plugin = "org.jetbrains.dokka")
    val dokkaPlugin by configurations
    dependencies { dokkaPlugin(rootProject.libs.dokka.versioning.plugin) }

    tasks.withType<DokkaTaskPartial>().configureEach {
        outputDirectory.set(layout.buildDirectory.dir("docs"))
        pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
            suppressInheritedMembers = true
            homepageLink = repositoryUrl
            footerMessage = copyright()
        }
    }
}

val repositoryUrl = "https://github.com/zed-alpha/shadow-gadgets"

fun copyright() = "© ${Year.now().value} zed-alpha"