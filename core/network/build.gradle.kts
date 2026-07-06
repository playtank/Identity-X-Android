import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.apollo3)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

// Fallback to emulator loopback if DEBUG_BASE_URL is not set in local.properties
val debugBaseUrl = localProps.getProperty("DEBUG_BASE_URL", "http://10.0.2.2:8080")

android {
    namespace = "com.identityx.android.core.network"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        // Debug: reads from local.properties → DEBUG_BASE_URL
        // Falls back to 10.0.2.2:8080 (emulator loopback) if not set
        getByName("debug") {
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
        }

        // Release: points to production server
        getByName("release") {
            buildConfigField("String", "BASE_URL", "\"https://api.identityx.com\"")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        jvmToolchain(21)
    }
}

dependencies {
    implementation(project(":core:local"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.play.services.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Ktor client
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json.client)

    // OkHttp (available for interceptors, caching, or custom engine use)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Apollo GraphQL client
    implementation(libs.apollo.runtime)
}

apollo {
    service("identityx") {
        packageName.set("com.identityx.android.core.network.graphql.generated")
        srcDir("src/main/graphql")
    }
}
