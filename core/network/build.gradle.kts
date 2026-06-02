plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.apollo3)
}

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
        // Debug: points to local Ktor MockServer (10.0.2.2 is the Android emulator loopback to host)
        getByName("debug") {
            buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8080\"")
        }

        // Release: points to production server
        getByName("release") {
            buildConfigField("String", "BASE_URL", "\"https://api.identityx.com\"")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.play.services.auth)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Ktor core and CIO engine, content negotiation, and logging for Ktor client Flow
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.logging)

    // Apollo GraphQL
    implementation(libs.apollo.runtime)

    // coroutines
    implementation(libs.kotlinx.coroutines.core)
}

apollo {
    service("service") {
        packageName.set("com.identityx.android.core.network.graphql")
    }
}
