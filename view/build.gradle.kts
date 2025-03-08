plugins {
    alias(libs.plugins.shadowgadgets.android.library)
    alias(libs.plugins.shadowgadgets.publish)
}

android {
    namespace = "com.zedalpha.shadowgadgets.view"

    buildFeatures {
        buildConfig = true
    }
}

dependencies {

    implementation(projects.core)
    lintPublish(projects.view.lint)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.components)
}