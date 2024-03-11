import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTaskPartial
import java.time.Year

plugins {
    id("com.android.library") version "8.3.0" apply false
    id("com.android.application") version "8.3.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("org.jetbrains.dokka") version "1.9.20"
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.20")
    }
}

tasks.dokkaHtmlMultiModule {
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