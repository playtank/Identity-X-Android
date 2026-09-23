plugins {
    // KGP — provides the kotlin {} DSL, all KMP targets and source sets
    alias(libs.plugins.kotlin.multiplatform)
    // AGP 9's dedicated KMP library plugin — provides the android {} block inside kotlin {}
    // NOTE: com.google.devtools.ksp and com.google.dagger.hilt.android are intentionally
    // NOT applied here. As of mid-2026, both KSP (google/ksp#2476) and Hilt
    // (google/dagger#4834) are incompatible with com.android.kotlin.multiplatform.library.
    // Hilt @Module wiring for this module's types must live in the consuming Android
    // module (e.g. :feature:login or :app-handheld) where plain com.android.library + KSP
    // are still fully supported.
    alias(libs.plugins.android.kotlin.multiplatform.library)
    kotlin("native.cocoapods")
}

kotlin {
    // ── Android target ────────────────────────────────────────────────────────
    android {
        namespace  = "com.identityx.auth_domain"
        compileSdk = 36
        minSdk     = 30

        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
        }

        // Consumer keep rules — optimization {} replaces consumerProguardFiles in the new plugin
        optimization {
            consumerKeepRules.apply {
                publish = true
                file("consumer-rules.pro")
            }
        }
    }

    // ── JVM target ────────────────────────────────────────────────────────────
    // Lets the :backend module (and future JVM consumers) import the domain
    // layer without pulling in AGP.
    jvm()

    // ── iOS Targets ───────────────────────────────────────────────────────────
    // 2. 添加 iOS Apple 平台编译目标
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    // ── CocoaPods Config ──────────────────────────────────────────────────────
    // 3. 👈 添加 CocoaPods 导出配置，生成 SharedAuthDomain.podspec
    cocoapods {
        summary = "Identity-X Auth Domain KMP Module"
        homepage = "https://github.com/your-repo/Identity-X"
        version = "1.0.0"
        ios.deploymentTarget = "15.0"

        framework {
            baseName = "SharedAuthDomain"
            isStatic = true
        }
    }
    // ── Source sets ───────────────────────────────────────────────────────────
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
