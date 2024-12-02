import java.time.Year

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.lint) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.dokka)
}

val documentedModules = listOf("view", "compose")
val documentedProjects = documentedModules.map { project(":$it") }

dependencies {
    documentedProjects.forEach { dokka(it) }
    dokkaHtmlPlugin(libs.dokka.versioning.plugin)
}

dokka {
    dokkaPublicationDirectory = rootDir.resolve("docs")

    pluginsConfiguration {
        html {
            customAssets.from(file("images/logo-icon.svg"))
            homepageLink = findProperty("repository.url")!!.toString()
            footerMessage =
                "© ${Year.now().value} ${findProperty("developer.name")!!}"
        }
        versioning {
            olderVersionsDir = rootDir.resolve("previous")
            version = findProperty("library.version")!!.toString()
        }
    }
}

configure(documentedProjects) {
    apply(plugin = rootProject.libs.plugins.dokka.get().pluginId)

    dependencies {
        dokkaHtmlPlugin(rootProject.libs.dokka.versioning.plugin)
    }

    dokka {
        dokkaPublicationDirectory = layout.buildDirectory.dir("docs")
        dokkaPublications.html { suppressInheritedMembers = true }
    }
}