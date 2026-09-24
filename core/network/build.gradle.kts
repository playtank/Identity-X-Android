import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val debugBaseUrl = localProps.getProperty("DEBUG_BASE_URL", "http://10.0.2.2:8080")

android {
    namespace = "com.identityx.android.core.network.shell"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        getByName("debug") {
            buildConfigField("String", "BASE_URL", "\"$debugBaseUrl\"")
            buildConfigField("Boolean", "ENABLE_NETWORK_LOGGING", "true")
        }
        getByName("release") {
            buildConfigField("String", "BASE_URL", "\"https://api.identityx.com\"")
            buildConfigField("Boolean", "ENABLE_NETWORK_LOGGING", "false")
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
    // Re-export the KMP module — all existing consumers keep compiling unchanged
    api(project(":shared:network"))

    // shared:local is a transitive dep via shared:network, but NetworkModule.kt
    // directly imports TokenProvider from com.identityx.local.domain — needs to
    // be explicit on this module's own compile classpath
    implementation(project(":shared:local"))

    // Hilt — plugin runs here, not in :shared:network
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // NetworkModule.kt directly references these — must be on this module's compile classpath
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.mock)
    implementation(libs.ktor.serialization.kotlinx.json.client)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.apollo.runtime)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.androidx.core.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
