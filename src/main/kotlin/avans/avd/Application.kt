package avans.avd

import avans.avd.auth.JwtConfig
import avans.avd.auth.JwtService
import avans.avd.auth.authModule
import avans.avd.incidents.FakeIncidentRepository
import avans.avd.incidents.IncidentService
import avans.avd.incidents.incidentsModule
import avans.avd.plugins.configureStatusPages
import avans.avd.users.FakeUserRepository
import avans.avd.users.UserService
import avans.avd.users.usersModule
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import java.io.File

@Serializable
data class AppConfig(
    val jwt: JwtConfig
)

fun main(args: Array<String>) {

    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    // Configure error handling (e.g., custom error pages)
    configureStatusPages()

    val userService = UserService(FakeUserRepository)
    val incidentService = IncidentService(FakeIncidentRepository)
    val appConfig = environment.config.getAs<AppConfig>()
    val jwtService = JwtService(appConfig.jwt, userService)

    install(ContentNegotiation) {
        json()
    }

    // Install route modules with explicit dependencies (no DI container)
    authModule(jwtService)
    incidentsModule(incidentService)
    usersModule(userService, incidentService)

    routing {
        staticFiles("/uploads", File("uploads"))}
}



