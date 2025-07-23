package avans.avd.users

import avans.avd.auth.UserPrincipal
import avans.avd.incidents.Incident
import avans.avd.incidents.IncidentService
import avans.avd.incidents.toResponse
import avans.avd.utils.assertHasRole
import avans.avd.utils.isQualifiedOfficial
import avans.avd.utils.userId
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
            val createUserRequest = call.receive<CreateUserRequest>()
            val createdUser = userService.save(
                user = createUserRequest.toModel()
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



        put("/{id}/role") {
            assertHasRole(Role.ADMIN)
            val id: Long = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val roleRequest = call.receive<RoleUpdateRequest>()
            val user = userService.findById(id)
                ?: throw NotFoundException("User with id $id not found")

            val updatedUser = user.copy(role = roleRequest.role)
            val savedUser = userService.save(updatedUser)
            call.respond(savedUser.toResponse())
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

        // Allow users to update their own details
        put("/me") {
            // Get the current authenticated user
            val userPrincipal = call.principal<UserPrincipal>()
            
            userPrincipal?.let {
                // Get the update request
                val updateRequest = call.receive<UpdateUserRequest>()

                // Get the current user
                val currentUser = userService.findById(userPrincipal.user.id)
                    ?: throw NotFoundException("User not found")

                val updatedUser = currentUser.copy(
                    username = updateRequest.username ?: currentUser.username,
                    // Only update password if provided and not empty
                    password = updateRequest.password?.takeIf { it.isNotBlank() } ?: currentUser.password,
                    email = updateRequest.email ?: currentUser.email,
                    // Don't allow users to change their own role
                    avatar = updateRequest.avatar ?: currentUser.avatar
                )

                // Save the updated user
                val savedUser = userService.save(updatedUser)

                // Return the updated user
                call.respond(savedUser.toResponse())
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
        avatar = this.avatar ?: "kodee.png",
        id = this.id.toString(),
    )

private fun CreateUserRequest.toModel(): User =
    User(
        id = User.NEW_USER_ID,
        username = this.username,
        password = this.password,
        email = this.email,
        avatar = this.avatar,
        role = Role.USER,
    )

