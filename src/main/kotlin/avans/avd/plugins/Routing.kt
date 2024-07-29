package avans.avd.plugins

import avans.avd.routes.authRoute
import avans.avd.routes.incidentRoute
import avans.avd.routes.userRoute
import avans.avd.services.IncidentService
import avans.avd.services.JwtService
import avans.avd.services.UserService
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    jwtService: JwtService,
    userService: UserService,
    incidentService: IncidentService
) {
    routing {
        route("/api/auth") {
            authRoute(jwtService)
        }
        route("/api/user") {
            userRoute(userService, incidentService)
        }
        route("/api/incident") {
            incidentRoute(incidentService)
        }
    }
}
