plugins {
    id("com.android.library")
}

android {
    namespace = "com.zedalpha.shadowgadgets.stubs"
    compileSdk = 34

    defaultConfig {
        minSdk = 21
    }
}

dependencies {
    api("androidx.annotation:annotation:1.7.0")
}