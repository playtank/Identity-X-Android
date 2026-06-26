plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.21"
    application
}

import java.util.Properties

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

// ---------------------------------------------------------------------------
// Read secrets from local.properties (never committed to version control)
// and inject them into a generated backend.properties resource file.
// ---------------------------------------------------------------------------

abstract class GenerateBackendPropertiesTask : DefaultTask() {
    @get:Input
    abstract val neonUrl: Property<String>
    @get:Input
    abstract val neonUser: Property<String>
    @get:Input
    abstract val neonPassword: Property<String>

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val propsFile = outputDir.get().file("backend.properties").asFile
        propsFile.parentFile.mkdirs()
        propsFile.writeText(
            """
            neon.url=${neonUrl.get()}
            neon.user=${neonUser.get()}
            neon.password=${neonPassword.get()}
            """.trimIndent()
        )
    }
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

val generateBackendProperties = tasks.register<GenerateBackendPropertiesTask>("generateBackendProperties") {
    neonUrl.set(localProps.getProperty("NEON_URL", ""))
    neonUser.set(localProps.getProperty("NEON_USER", ""))
    neonPassword.set(localProps.getProperty("NEON_PASSWORD", ""))
    outputDir.set(layout.buildDirectory.dir("generated/backend-resources/main"))
}

sourceSets["main"].resources.srcDir(generateBackendProperties.flatMap { it.outputDir })

tasks.named("processResources") {
    dependsOn(generateBackendProperties)
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
