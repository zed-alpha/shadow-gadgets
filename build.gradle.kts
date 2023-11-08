import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.time.Year

plugins {
    id("com.android.library") version "8.1.2" apply false
    id("com.android.application") version "8.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("org.jetbrains.dokka") version "1.9.10"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.10")
    }
}

val footer = "© ${Year.now().value} ZedAlpha"

tasks.dokkaHtmlMultiModule {
    outputDirectory.set(rootProject.file("docs"))
    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        customAssets = listOf(file("images/logo-icon.svg"))
        footerMessage = footer
    }
}

subprojects {
    if (name == "compose" || name == "view") {
        apply(plugin = "org.jetbrains.dokka")
        tasks.withType<DokkaTaskPartial>().configureEach {
            outputDirectory.set(file("$buildDir/docs"))
            pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
                footerMessage = footer
                suppressInheritedMembers = true
            }
        }
    }
}