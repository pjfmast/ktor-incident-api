val kotlin_version: String by project
val logback_version: String by project
val exposedVersion: String by project
val h2_version: String by project

plugins {
    kotlin("jvm") version "2.2.20"
    id("io.ktor.plugin") version "3.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
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
    implementation("ch.qos.logback:logback-classic:$logback_version")

    // needed for class Incident
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

    // added for persistence with Exposed
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("com.h2database:h2:$h2_version")


    testImplementation(ktorLibs.server.testHost)
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
}
