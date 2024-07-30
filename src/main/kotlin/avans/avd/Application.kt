package avans.avd

import avans.avd.incidents.FakeIncidentRepository
import avans.avd.users.FakeUserRepository
import avans.avd.incidents.IncidentService
import avans.avd.auth.JwtService
import avans.avd.auth.authModule
import avans.avd.exceptions.MissingRoleException
import avans.avd.incidents.incidentsModule
import avans.avd.users.UserService
import avans.avd.users.usersModule
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
fun Application.module() {
    val userService = UserService(FakeUserRepository)
    val incidentService = IncidentService(FakeIncidentRepository)
    val jwtService = JwtService(this, userService)

    install(Koin) {
        modules(module(createdAtStart = true, fun Module.() {
            single { userService }
            single { jwtService }
            single { incidentService }
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
