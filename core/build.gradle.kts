plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

android {
    namespace = "com.zedalpha.shadowgadgets.core"
    compileSdk = 33

    defaultConfig {
        minSdk = 21
    }
    buildTypes.all {
        consumerProguardFiles("consumer-rules.pro")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    publishing {
        singleVariant("release")
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.zedalpha.shadowgadgets"
                artifactId = "core"
                version = findProperty("library.version").toString()
            }
        }
    }
}

dependencies {
    compileOnly(project(":stubs"))
    implementation("androidx.core:core-ktx:1.10.1")
}