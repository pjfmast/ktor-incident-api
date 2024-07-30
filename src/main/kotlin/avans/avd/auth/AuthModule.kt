package avans.avd.auth

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.authModule() {
    val jwtService: JwtService by inject()

    configureSecurity(jwtService)

    routing {
        route("/api/auth") {
            authRoute(jwtService)
        }
    }
}