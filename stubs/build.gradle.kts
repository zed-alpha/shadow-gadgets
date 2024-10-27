plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "com.zedalpha.shadowgadgets.stubs"
    compileSdk = 35

    defaultConfig {
        minSdk = 21
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    api(libs.androidx.annotation)
}