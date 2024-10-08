package avans.avd.users

import avans.avd.incidents.IncidentService
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File

@Suppress("unused")
fun Application.usersModule() {
    val userService: UserService by inject()
    val incidentService: IncidentService by inject()

    routing {
        staticFiles("api/users/images", File("uploads/usersImages"), "kodee.png") {
            default("kodee.png")
        }
        route("api/users") {
            userRoutes(userService, incidentService)
        }
    }
}