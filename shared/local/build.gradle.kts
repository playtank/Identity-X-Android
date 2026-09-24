plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    // NOTE: ksp and dagger.hilt.android are intentionally absent.
    // As of AGP 9.x, KSP (google/ksp#2476) and Hilt (google/dagger#4834) are
    // incompatible with com.android.kotlin.multiplatform.library.
    // Room KSP processing and Hilt @Module wiring live in :core:local, which
    // is a plain com.android.library that depends on this module.
}

kotlin {
    android {
        namespace  = "com.identityx.local"
        compileSdk = 36
        minSdk     = 30

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }

        optimization {
            consumerKeepRules.apply {
                publish = true
                file("consumer-rules.pro")
            }
        }
    }

    // JVM target — lets :backend or future desktop consumers import
    // TokenProvider / UserPreferencesProvider without pulling in AGP.
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        androidMain.dependencies {
            // Room runtime (no KSP here — compiler runs in :core:local)
            implementation(libs.room.runtime)
            implementation(libs.room.ktx)
            // Encrypted token storage
            implementation(libs.androidx.security.crypto)
            implementation(libs.androidx.core.ktx)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }
    }
}
