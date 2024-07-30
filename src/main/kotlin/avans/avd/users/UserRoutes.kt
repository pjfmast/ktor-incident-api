package avans.avd.users

import avans.avd.incidents.Incident
import avans.avd.incidents.IncidentService
import avans.avd.incidents.toResponse
import avans.avd.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
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
        get {
            assertHasRole(Role.ADMIN)
            val users = userService.findAll()
            call.respond(users.map(User::toResponse))
        }
        route("/{id}") {
            get {
                val id: Long = call.parameters["id"]?.toLongOrNull()
                    ?: throw BadRequestException("Invalid ID")

                val foundUser = userService.findById(id)
                    ?: throw NotFoundException("User with id $id not found")

                // an Admin may get any user
                val isAdmin = call.userRole() == Role.ADMIN
                if (!isAdmin && foundUser.username != call.userName())
                    throw NotFoundException()

                call.respond(foundUser.toResponse())
            }

            get("/incident") {
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