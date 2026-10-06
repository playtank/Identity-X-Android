plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dagger.hilt.android)
}

android {
    namespace = "com.identityx.local.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 30
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

ksp {
    arg("room.generateKotlin", "true")
}

dependencies {
    // Re-export the KMP module so all existing consumers keep compiling unchanged
    api(project(":shared:local"))

    // LocalDataModule.kt directly references these — must be on this module's compile classpath
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.core.ktx)

    // Room KSP compiler — runs here, not in :shared:local (KSP incompatible with KMP plugin)
    ksp(libs.room.compiler)

    // Hilt — same reason
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
