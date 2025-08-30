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
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

/**
 * Common Ktor test application setup so tests don't repeat DI and plugin wiring.
 */
fun Application.installTestModules() {
    val testModule: Module = module(createdAtStart = true) {
        single<UserService> { UserService(FakeUserRepository) }
        single<JwtService> {
            JwtService(
                "my secret",
                "http://localhost",
                "my-audience",
                "my realm",
                get()
            )
        }
        single<IncidentService> { IncidentService(FakeIncidentRepository) }
    }
    install(Koin) { modules(testModule) }
    install(ContentNegotiation) { json() }
    // Install routes and security commonly needed by tests
    authModule()
    incidentsModule()
}
