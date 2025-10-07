package avans.avd.auth

import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoute(jwtService: JwtService) {
    post("/login") {
        // note: call.receive<> consumes the request body so cannot be called twice.
        val loginRequest = call.receive<LoginRequest>()

        val token = jwtService.authenticate(loginRequest)

        token?.let {
            call.respond(hashMapOf("token" to it))
        } ?: call.respond(HttpStatusCode.Unauthorized)
    }
}