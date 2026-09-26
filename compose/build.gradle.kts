plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.vanniktech.publish)
    alias(libs.plugins.dokka)
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

mavenPublishing {
    if (System.getenv("JITPACK") == "true") {
        logger.quiet("JitPack build; skipping signing.")
    } else {
        signAllPublications()
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)

    lintPublish(projects.compose.lint)
}