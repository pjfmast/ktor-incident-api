@Suppress("DSL_SCOPE_VIOLATION")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlin.serialization)
}

group = "avans.avd"
version = "0.0.1"

application {
    mainClass.set("avans.avd.ApplicationKt")

//    val isDevelopment: Boolean = project.ext.has("development")
    val isDevelopment = providers.gradleProperty("development").isPresent
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    jvmToolchain(21)
    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
    }
}

dependencies {
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(libs.logback.classic)

    // needed for class Incident
    implementation(libs.kotlinx.datetime)

    // added for persistence with Exposed
    implementation(libs.exposed.dao)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.h2)


    testImplementation(ktorLibs.server.testHost)
    testImplementation(libs.kotlin.test.junit)
}
