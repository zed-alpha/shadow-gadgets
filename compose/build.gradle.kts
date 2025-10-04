import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    id("maven-publish")
}

android {
    namespace = "com.zedalpha.shadowgadgets.compose"
    defaultConfig.minSdk = 23

    compileSdk {
        version = release(36)
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
        explicitApi()
    }
}

afterEvaluate {
    publishing.publications {
        create<MavenPublication>("release") {
            from(components["release"])
            groupId = findProperty("group.id")!!.toString()
            artifactId = "compose"
            version = findProperty("library.version")!!.toString()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
}