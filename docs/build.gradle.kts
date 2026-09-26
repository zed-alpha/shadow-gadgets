import org.jetbrains.dokka.gradle.DokkaExtension
import java.time.Year

plugins {
    alias(libs.plugins.dokka)
}

val targets = listOf(projects.compose, projects.view)

dependencies {
    targets.forEach { dokka(it) }
    dokkaHtmlPlugin(libs.dokka.versioning.plugin)
}

val footer = "© ${Year.now().value} ${stringProperty("POM_DEVELOPER_NAME")}"
val repoUrl = stringProperty("POM_URL")

dokka {
    moduleName = "Shadow Gadgets"
    basePublicationsDirectory = project.layout.projectDirectory

    pluginsConfiguration {
        html {
            customAssets.from(file("../images/logo-icon.svg"))
            footerMessage = footer
            homepageLink = repoUrl
        }
        versioning {
            olderVersionsDir = basePublicationsDirectory.dir("previous")
            version = stringProperty("VERSION_NAME")
        }
    }
}

configure(targets) {
    val target = project(this.path)
    target.pluginManager.withPlugin(libs.plugins.dokka.get().pluginId) {
        target.extensions.configure<DokkaExtension> {
            dokkaPublications.html {
                suppressInheritedMembers = true
            }

            dokkaSourceSets.configureEach {
                pluginsConfiguration {
                    html {
                        footerMessage = footer
                        homepageLink = repoUrl
                    }
                }

                sourceLink {
                    localDirectory = project.layout.projectDirectory.dir("src")
                    remoteUrl = uri("$repoUrl/tree/main/${project.name}/src")
                    remoteLineSuffix = "#L"
                }
            }
        }
    }
}

fun Project.stringProperty(name: String): String =
    this.providers.gradleProperty(name).get()