rootProject.name = "ktor-incident-api"

dependencyResolutionManagement {
    repositories { mavenCentral() }

    versionCatalogs {
        create("ktorLibs") {
            from("io.ktor:ktor-version-catalog:3.3.0")
        }
    }
}
