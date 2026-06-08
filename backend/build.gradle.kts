plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    application
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

application {
    mainClass.set("com.identityx.backend.ApplicationKt")
}

dependencies {
    // Ktor server engine (Netty)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    // Content negotiation and JSON serialization
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // PostgreSQL JDBC driver (Neon cloud database)
    implementation(libs.postgresql)

    // Logging
    implementation(libs.logback.classic)
}
