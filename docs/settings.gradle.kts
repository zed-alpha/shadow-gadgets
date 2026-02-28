dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositoriesMode = RepositoriesMode.PREFER_SETTINGS

    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
    }

    versionCatalogs {
        create("libs").from(files("../gradle/libs.versions.toml"))
    }
}

rootProject.name = "docs"

includeBuild("..")