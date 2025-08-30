package avans.avd

import avans.avd.auth.JwtService
import avans.avd.incidents.FakeIncidentRepository
import avans.avd.incidents.IncidentService
import avans.avd.users.FakeUserRepository
import avans.avd.users.UserService
import configureStatusPages
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    // Configure error handling (e.g., custom error pages)
    configureStatusPages()

    val userService = UserService(FakeUserRepository)
    val incidentService = IncidentService(FakeIncidentRepository)
    val jwtService = JwtService(this, userService)

    // see: https://insert-koin.io/docs/reference/koin-ktor/ktor/
    // In Ktor we use 'install(Koin) {...}' instead of startKoin {...}
    install(Koin) {
        // modules contain declarations of dependencies between services, resources, and repositories.
        // By default, lazy creation. To apply eager creation, use createdAtStart = true
        modules(module(createdAtStart = true, fun Module.() {
            // The single<T> {} will create a definition for a singleton object of type T
            // and will return this same instance each time get() is called.
            // The explicit type in <T> can be omitted.
            single<UserService> { userService }
            single<JwtService> { jwtService }
            single<IncidentService> { incidentService }
        }))
    }
    install(ContentNegotiation) {
        json()
    }

}



