package avans.avd.auth

import io.ktor.server.application.*
import io.ktor.server.routing.*

@Suppress("unused")
fun Application.authModule(jwtService: JwtService) {
    configureSecurity(jwtService)

    routing {
        route("/api/auth") {
            authRoute(jwtService)
        }
    }
}