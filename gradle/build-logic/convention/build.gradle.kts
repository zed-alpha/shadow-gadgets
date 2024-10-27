import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradle.plugin)
    compileOnly(libs.kotlin.gradle.plugin)
    compileOnly(libs.compose.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidLibrary") {
            id = "shadowgadgets.android.library"
            implementationClass = "AndroidLibraryConventionPlugin"
        }
        register("publish") {
            id = "shadowgadgets.publish"
            implementationClass = "PublishConventionPlugin"
        }
    }
}