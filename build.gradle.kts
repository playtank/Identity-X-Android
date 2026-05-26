// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false // 新增的 Compose 编译器插件
    alias(libs.plugins.dagger.hilt.android) apply false   // 对应的 Hilt 别名
}
//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//    }
//    dependencies {
//        // 强行把所有核心插件的版本锁死在最稳定的低版本
//        classpath("com.android.tools.build:gradle:8.2.2")
//        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
//        classpath("com.google.dagger:hilt-android-gradle-plugin:2.50")
//    }
//}
//
//tasks.register("clean", Delete::class) {
//    delete(rootProject.layout.buildDirectory)
//}