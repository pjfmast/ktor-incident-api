package avans.avd

import avans.avd.plugins.*
import avans.avd.repositories.FakeIncidentRepository
import avans.avd.repositories.FakeUserRepository
import avans.avd.services.IncidentService
import avans.avd.services.JwtService
import avans.avd.services.UserService
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

@Suppress("unused")
fun Application.module() {
    val userService = UserService(FakeUserRepository)
    val incidentService = IncidentService(FakeIncidentRepository)
    val jwtService = JwtService(this, userService)

    configureSecurity(jwtService)
    configureSerialization()
    configureRouting(jwtService, userService, incidentService)
}
