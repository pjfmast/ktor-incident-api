package avans.avd.routes

import avans.avd.dto.UserRequest
import avans.avd.dto.UserResponse
import avans.avd.models.Incident
import avans.avd.models.Role
import avans.avd.models.User
import avans.avd.models.isQualifiedOfficial
import avans.avd.plugins.extractRoleFromToken
import avans.avd.services.IncidentService
import avans.avd.services.UserService
import avans.avd.util.authorized
import avans.avd.util.extractPrincipalId
import avans.avd.util.extractPrincipalName
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoute(
    userService: UserService,
    incidentService: IncidentService
) {
    post {
        val userRequest = call.receive<UserRequest>()

        val createdUser = userService.save(
            user = userRequest.toModel()
        )

        call.response.header(
            name = "id",
            value = createdUser.id.toString()
        )
        call.respond(HttpStatusCode.Created)
    }

    // Only ADMIN can get all Users
    authenticate {
        authorized(Role.ADMIN) {
            get {
                val users = userService.findAll()
                call.respond(users.map(User::toResponse))
            }
        }
    }

    authenticate {
        route("/{id}") {
            get {
                val id: Long = call.parameters["id"]?.toLong()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val foundUser = userService.findById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound)

                // an Admin may get any user
                val isAdmin = extractRoleFromToken(call) == Role.ADMIN
                if (!isAdmin && foundUser.username != extractPrincipalName(call)) {
                    return@get call.respond(HttpStatusCode.NotFound)
                }

                call.respond(foundUser.toResponse())
            }

            get("/incident") {
                val id: Long = call.parameters["id"]?.toLong()
                    ?: return@get call.respond(HttpStatusCode.BadRequest)

                val foundUser = userService.findById(id)
                    ?: return@get call.respond(HttpStatusCode.NotFound)

                val role = extractRoleFromToken(call)
                if (!role.isQualifiedOfficial() && foundUser.id != extractPrincipalId(call)) {
                    return@get call.respond(HttpStatusCode.NotFound)
                }
                val foundIncidentsOfUser = incidentService.findIncidentsReportedByUser(foundUser.id)
                call.respond(foundIncidentsOfUser.map(Incident::toResponse))
            }
        }
    }
}

private fun User.toResponse(): UserResponse =
    UserResponse(
        id = this.id.toString(),
        username = this.username,
        email = this.email,
        role = this.role
    )

private fun UserRequest.toModel(): User =
    User(
        id = User.NEW_USER_ID,
        username = this.username,
        password = this.password,
        email = this.email,
        role = Role.USER,
    )