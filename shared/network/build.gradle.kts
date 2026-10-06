plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.apollo3)
    // NOTE: ksp and dagger.hilt.android are intentionally absent.
    // KSP (google/ksp#2476) and Hilt (google/dagger#4834) are incompatible
    // with com.android.kotlin.multiplatform.library as of AGP 9.x.
    // NetworkModule (Hilt @Module) lives in :core:network, a plain android.library.
}

kotlin {
    android {
        namespace  = "com.identityx.android.core.network"
        compileSdk = 36
        minSdk     = 24

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

    // JVM target — consumed by :backend (Ktor server)
    jvm()

    // ── iOS Targets ───────────────────────────────────────────────────────────
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":shared:local"))
            implementation(libs.kotlinx.coroutines.core)

            // javax.inject — provides @Inject, @Singleton, @Named annotations.
            // NOTE: only moved to androidMain/jvmMain — iOS uses no-op stubs in iosMain.

            // Ktor client — engine-agnostic; engines wired per-target below
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.serialization.kotlinx.json.client)
            implementation(libs.ktor.client.mock)

            // Apollo GraphQL — KMP-compatible runtime
            implementation(libs.apollo.runtime)
        }

        androidMain.dependencies {
            // OkHttp engine for Android
            implementation(libs.ktor.client.okhttp)
            implementation(libs.okhttp)
            implementation(libs.okhttp.logging.interceptor)
            implementation(libs.androidx.core.ktx)
            // Hilt runtime needed for @Inject annotations in NetworkMonitor
            // (no Hilt plugin here — the @Module wiring lives in :core:network)
            implementation(libs.hilt.android)
            // JSR-330 DI annotations — Android/JVM only; iOS uses no-op stubs
            implementation(libs.javax.inject)
        }

        jvmMain.dependencies {
            // CIO engine for the :backend JVM target
            implementation(libs.ktor.client.cio)
            // JSR-330 DI annotations
            implementation(libs.javax.inject)
        }

        // Darwin engine for iOS — NSURLSession-backed, required for all Apple targets
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

apollo {
    service("identityx") {
        packageName.set("com.identityx.android.core.network.graphql.generated")
        srcDir("src/commonMain/graphql")
    }
}
