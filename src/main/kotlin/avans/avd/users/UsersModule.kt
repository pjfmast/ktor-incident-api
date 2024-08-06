package avans.avd.users

import avans.avd.incidents.IncidentService
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.usersModule() {
    val userService: UserService by inject()
    val incidentService: IncidentService by inject()

    routing {
        route("api/users") {
            userRoutes(userService, incidentService)
        }
    }
}