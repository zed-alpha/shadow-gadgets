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
    implementation(project(":core"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material.components)
    lintPublish(project(":view:lint"))
}