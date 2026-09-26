plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dokka)
}

if (providers.gradleProperty("enablePublishing").orNull == "true") {
    pluginManager.apply(libs.plugins.vanniktech.publish.get().pluginId)
}

android {
    namespace = "com.zedalpha.shadowgadgets.compose"

    compileSdk {
        version = release(37)
    }
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    explicitApi()
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)

    if (System.getenv("JITPACK") == "true") {
        logger.quiet("JitPack build: adding move bulletin to :$name.")
        lintPublish(projects.bulletin)
    }
}