package avans.avd

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

/**
 * Common Ktor test application setup so tests don't repeat DI and plugin wiring.
 */
fun Application.installTestModules() {
    // Manual wiring in tests
    val userService = UserService(FakeUserRepository)
    val incidentService = IncidentService(FakeIncidentRepository)
    val jwtService = JwtService(
        "my secret",
        "http://localhost",
        "my-audience",
        "my realm",
        userService
    )

    install(ContentNegotiation) { json() }

    // Install routes and security commonly needed by tests
    authModule(jwtService)
    incidentsModule(incidentService)
}
