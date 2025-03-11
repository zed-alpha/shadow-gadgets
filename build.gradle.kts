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

val documentedProjects = listOf(projects.view, projects.compose)

dependencies {
    documentedProjects.forEach { dokka(it) }
    dokkaHtmlPlugin(libs.dokka.versioning.plugin)
}

dokka {
    basePublicationsDirectory = rootDir.resolve("docs")

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
        basePublicationsDirectory = layout.buildDirectory.dir("docs")
        dokkaPublications.html { suppressInheritedMembers = true }
    }
}