plugins {
    alias(libs.plugins.android.library)
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

dependencies {
    compileOnly(projects.stubs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.components)

    testImplementation(libs.junit)

    lintPublish(projects.view.lint)
}