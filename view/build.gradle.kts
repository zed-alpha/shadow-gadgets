import java.time.Year

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
    id("maven-publish")
}

android {
    namespace = "com.zedalpha.shadowgadgets.view"

    defaultConfig.minSdk = 21
    buildFeatures.buildConfig = true

    compileSdk {
        version = release(36) { minorApiLevel = 1 }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        release { consumerProguardFiles("consumer-rules.pro") }
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

kotlin {
    explicitApi()
}

afterEvaluate {
    publishing.publications {
        create<MavenPublication>("release") {
            from(components["release"])
            groupId = findProperty("group.id")!!.toString()
            artifactId = "view"
            version = findProperty("library.version")!!.toString()
        }
    }
}

project.group = requireProperty("group.id")
project.version = requireProperty("library.version")

dokka {
    dokkaSourceSets.configureEach {
        pluginsConfiguration {
            html {
                homepageLink = requireProperty("repository.url")
                footerMessage =
                    "© ${Year.now().value} ${requireProperty("developer.name")}"
            }
        }

        sourceLink {
            localDirectory = project.layout.projectDirectory.dir("src")
            val repoUrl = requireProperty("repository.url")
            remoteUrl = uri("$repoUrl/tree/main/${project.name}/src")
            remoteLineSuffix = "#L"
        }
    }
}

fun Project.requireProperty(name: String): String =
    requireNotNull(this.properties[name]) { "Cannot find property: $name" }
        .toString()

dependencies {
    compileOnly(projects.stubs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.components)

    testImplementation(libs.junit)

    lintPublish(projects.view.lint)
}