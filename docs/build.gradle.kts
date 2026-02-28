import java.time.Year
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dokka)
}

dependencies {
    dokka(libs.shadowgagdgets.view)
    dokka(libs.shadowgagdgets.compose)
    dokkaHtmlPlugin(libs.dokka.versioning.plugin)
}

val rootProperties = Properties()
rootProperties.load(file("../gradle.properties").reader())

project.group = rootProperties.requireProperty("group.id")
project.version = rootProperties.requireProperty("library.version")

dokka {
    moduleName = "Shadow Gadgets"
    basePublicationsDirectory = project.layout.projectDirectory
    dokkaPublications.html { suppressInheritedMembers = true }

    pluginsConfiguration {
        html {
            customAssets.from(file("../images/logo-icon.svg"))
            homepageLink = rootProperties.requireProperty("repository.url")
            footerMessage =
                "© ${Year.now().value} " +
                        rootProperties.requireProperty("developer.name")
        }
        versioning {
            olderVersionsDir = basePublicationsDirectory.dir("previous")
            version = project.version.toString()
        }
    }
}

fun Properties.requireProperty(name: String): String =
    requireNotNull(this[name]) { "Cannot find property: $name" }.toString()