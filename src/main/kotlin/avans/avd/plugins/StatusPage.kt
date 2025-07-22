import avans.avd.exceptions.MissingRoleException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        // Exception handlers
        exception<Exception> { call, cause ->
            val response = when (cause) {
                is BadRequestException  -> HttpStatusCode.BadRequest to (cause.message ?: "Invalid request.")
                is NotFoundException    -> HttpStatusCode.NotFound to  (cause.message ?: "Resource not found.")
                is MissingRoleException -> HttpStatusCode.Forbidden to (cause.message ?: "You do not have permission to access this resource.")
                else                    -> HttpStatusCode.InternalServerError to "An unexpected error occurred."
            }
            call.respond(response.first, mapOf("error" to response.second))
        }

        // Status code handlers
        status(HttpStatusCode.Unauthorized) { call, status ->
            call.respond(status, mapOf("error" to "Authentication is required to access this resource"))
        }
    }
}