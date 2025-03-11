plugins {
    alias(libs.plugins.shadowgadgets.android.library)
    alias(libs.plugins.shadowgadgets.publish)
}

android {
    namespace = "com.zedalpha.shadowgadgets.core"
}

dependencies {

    compileOnly(projects.stubs)

    implementation(libs.androidx.core.ktx)
}