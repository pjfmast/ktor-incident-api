package avans.avd.routes

import avans.avd.dto.LoginRequest
import avans.avd.services.JwtService
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoute(jwtService: JwtService) {
    post {
        val loginRequest = call.receive<LoginRequest>()

        val token = jwtService.authenticate(loginRequest)

        token?.let {
            call.respond(hashMapOf("token" to it))
        } ?: call.respond(HttpStatusCode.Unauthorized)
    }
}