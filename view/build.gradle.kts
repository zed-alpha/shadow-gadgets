plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.dokka)
}

if (providers.gradleProperty("enablePublishing").orNull == "true") {
    pluginManager.apply(libs.plugins.vanniktech.publish.get().pluginId)
}

android {
    namespace = "com.zedalpha.shadowgadgets.view"

    compileSdk {
        version = release(37)
    }
    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        release { consumerProguardFiles("consumer-rules.pro") }
    }
}

kotlin {
    explicitApi()
}

dependencies {
    compileOnly(projects.stubs)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.components)

    testImplementation(libs.junit)

    lintPublish(projects.view.lint)

    if (System.getenv("JITPACK") == "true") {
        logger.quiet("JitPack build: adding move bulletin to :$name.")
        lintPublish(projects.bulletin)
    }
}