plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.android.lint)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.bundles.lint.api)
    // TODO: Causes a warning that it depends on IDE JARs. I don't see how.
    testImplementation(libs.bundles.lint.tests)
}