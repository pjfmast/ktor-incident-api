rootProject.name = "ktor-incident-api"

dependencyResolutionManagement {
    versionCatalogs {
        create("ktorLibs") {
            from("io.ktor:ktor-version-catalog:3.2.2")
        }
    }
    repositories { mavenCentral() }
}
