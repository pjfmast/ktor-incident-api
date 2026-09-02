package avans.avd

import avans.avd.auth.JwtConfig
import avans.avd.auth.JwtService
import avans.avd.auth.authModule
import avans.avd.core.DatabaseFactory
import avans.avd.incidents.*
import avans.avd.plugins.configureStatusPages
import avans.avd.users.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.http.content.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
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

    // Connect to the database and create the schema. Pass DatabaseFactory.H2_FILE_URL as the second
    // argument to keep the data between restarts instead of using the in-memory database.
    DatabaseFactory.init(listOf(UsersTable, IncidentsTable, IncidentImagesTable))

    // The repositories are plain classes, so this application owns its own instances.
    // Seeding is suspending, hence runBlocking during (one-time) startup wiring.
    // Both seedDemoData functions are idempotent: an already filled database is left untouched,
    // so with a file-based database the demo data is only inserted the very first time.
    val userRepository = ExposedUserRepository()
    val incidentRepository = ExposedIncidentRepository()
    runBlocking {
        userRepository.seedDemoData()
        incidentRepository.seedDemoData(userRepository)
    }

    val userService = UserService(userRepository)
    val incidentService = IncidentService(incidentRepository)
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
