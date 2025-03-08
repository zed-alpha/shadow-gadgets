pluginManagement {
    includeBuild("gradle/build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "shadow-gadgets"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(":stubs")
include(":core")
include(":view")
include(":view:lint")
include(":compose")
include(":demo")