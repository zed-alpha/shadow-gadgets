import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.time.Year

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.dokka) apply false
}

buildscript {
    dependencies {
        classpath(libs.dokka.base)
    }
}

tasks.withType<DokkaMultiModuleTask>().configureEach {
    outputDirectory.set(file("docs"))
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("images/logo-icon.svg"))
        footerMessage = footer()
        homepageLink = "https://github.com/zed-alpha/shadow-gadgets"
    }
}

subprojects {
    if (name == "compose" || name == "view") {
        apply(plugin = "org.jetbrains.dokka")
        tasks.withType<DokkaTaskPartial>().configureEach {
            outputDirectory.set(layout.buildDirectory.dir("docs").get())
            pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
                footerMessage = footer()
                homepageLink = "https://github.com/zed-alpha/shadow-gadgets"
                suppressInheritedMembers = true
            }
        }
    }
}

fun footer() = "© ${Year.now().value} zed-alpha"