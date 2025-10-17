plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
}
java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}
dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Hilt
    implementation(libs.dagger)
    implementation(libs.hilt.core)

    // JavaX Inject
    implementation(libs.javax.inject)
}
