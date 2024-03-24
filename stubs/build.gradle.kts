plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.zedalpha.shadowgadgets.stubs"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    api(libs.androidx.annotation)
}