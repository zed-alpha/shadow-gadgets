plugins {
    alias(libs.plugins.shadowgadgets.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.shadowgadgets.publish)
}

android {
    namespace = "com.zedalpha.shadowgadgets.compose"
}

dependencies {

    implementation(projects.core)

    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
}