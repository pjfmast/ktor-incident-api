package avans.avd

import avans.avd.auth.JwtConfig
import avans.avd.auth.JwtService
import avans.avd.auth.authModule
import avans.avd.incidents.FakeIncidentRepository
import avans.avd.incidents.IncidentService
import avans.avd.incidents.incidentsModule
import avans.avd.users.FakeUserRepository
import avans.avd.users.UserService
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.runBlocking

/**
 * Single JWT configuration shared by the test application and the test client helpers.
 */
val testJwtConfig = JwtConfig(
    secret = "my secret",
    issuer = "http://localhost",
    audience = "ktor-incident-api",
    realm = "my realm"
)

/**
 * Common Ktor test application setup so tests don't repeat DI and plugin wiring.
 */
fun Application.installTestModules() {
    // Manual wiring in tests: every test gets its own freshly seeded fakes,
    // so state never leaks from one test into another.
    val userRepository = runBlocking { FakeUserRepository.withDemoData() }
    val incidentRepository = runBlocking { FakeIncidentRepository.withDemoData(userRepository) }

    val userService = UserService(userRepository)
    val incidentService = IncidentService(incidentRepository)
    val jwtService = JwtService(testJwtConfig, userService)

    install(ContentNegotiation) { json() }

    // Install routes and security commonly needed by tests
    authModule(jwtService)
    incidentsModule(incidentService)
}
