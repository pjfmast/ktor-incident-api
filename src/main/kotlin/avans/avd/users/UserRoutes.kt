package avans.avd.users

import avans.avd.auth.UserPrincipal
import avans.avd.incidents.Incident
import avans.avd.incidents.IncidentService
import avans.avd.incidents.toResponse
import avans.avd.utils.*
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(
    userService: UserService,
    incidentService: IncidentService
) {
    // anyone can register as a user:
    route("/register") {
        post {
            val userRequest = call.receive<UserRequest>()
            val createdUser = userService.save(
                user = userRequest.toModel()
            )
            val userResponse = createdUser.toResponse()
            call.respond(HttpStatusCode.Created, userResponse)
        }
    }

    // user requests for ADMIN only
    authenticate {
        // Only ADMIN can get all Users
        get {
            assertHasRole(Role.ADMIN)
            val users = userService.findAll()
            call.respond(users.map(User::toResponse))
        }

        // Only ADMIN can get any user
        get("/{id}") {
            assertHasRole(Role.ADMIN)
            val id: Long = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val foundUser = userService.findById(id)
                ?: throw NotFoundException("User with id $id not found")

            call.respond(foundUser.toResponse())
        }

        delete("/{id}") {
            assertHasRole(Role.ADMIN)
            val id: Long = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val deleted = userService.delete(id)
            if (deleted) {
                call.respond(HttpStatusCode.NoContent, "User with id $id is deleted.")
            } else {
                throw NotFoundException("User with id $id not found")
            }
        }

    }

    // requests available for authenticated users:
    authenticate {
        // any authenticated user can retrieve the current user information
        get("/me") {
            // Get the current authenticated user from the principal
            val userPrincipal = call.principal<UserPrincipal>()

            userPrincipal?.let {
                // Return the user information (excluding sensitive data like password)
                call.respond(it.user.toResponse())
            } ?: call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
        }


        // any qualified official can retrieve incidents reported by a user,
        // other users can only retrieve their own reported incidents.
        get("{id}/incidents") {
            val id: Long = call.parameters["id"]?.toLong()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val foundUser = userService.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)

            if (!isQualifiedOfficial() && foundUser.id != call.userId())
                throw NotFoundException()

            val foundIncidentsOfUser = incidentService.findIncidentsReportedByUser(foundUser.id)
            call.respond(foundIncidentsOfUser.map(Incident::toResponse))
        }
    }
}

private fun User.toResponse(): UserResponse =
    UserResponse(
        username = this.username,
        email = this.email,
        role = this.role,
        avatar = "user${id}",
        id = this.id.toString(),
    )

private fun UserRequest.toModel(): User =
    User(
        id = User.NEW_USER_ID,
        username = this.username,
        password = this.password,
        email = this.email,
        role = Role.USER,
    )