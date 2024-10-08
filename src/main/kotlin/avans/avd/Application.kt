package avans.avd

import avans.avd.incidents.FakeIncidentRepository
import avans.avd.users.FakeUserRepository
import avans.avd.incidents.IncidentService
import avans.avd.auth.JwtService
import avans.avd.exceptions.MissingRoleException
import avans.avd.users.UserService
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import java.io.PrintStream

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.rootModule() {
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
    install(StatusPages) {
        exception<MissingRoleException> { call, cause ->
            call.respondText(ContentType.Text.Plain, HttpStatusCode.Forbidden) {
                cause.message ?: ""
            }
        }
        exception<RuntimeException> { call, cause ->
            call.respondOutputStream(ContentType.Text.Plain, HttpStatusCode.InternalServerError) {
                PrintStream(this).use { stream ->
                    cause.printStackTrace(stream)
                }
            }
        }
    }
}
