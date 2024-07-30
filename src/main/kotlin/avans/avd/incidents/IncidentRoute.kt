package avans.avd.incidents

import avans.avd.exceptions.MissingRoleException
import avans.avd.users.Role
import avans.avd.utils.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.incidentRoute(
    incidentService: IncidentService
) {
    // Anyone may create an Incident, if anonymous the Incident cannot be edited later by the issuer.
    // if an authenticated user creates an Incident, the userId identifies the user who created this Incident
    authenticate(optional = true) {
        post {
            val incidentRequest = call.receive<IncidentRequest>()
            val userId = call.userId()

            val createdIncident = incidentService.save(
                incidentRequest.toModel(Incident.NEW_INCIDENT_ID, userId)
            )

            call.response.header(
                name = "id",
                value = createdIncident.id.toString()
            )
            call.respond(HttpStatusCode.Created)
        }
    }


    authenticate {
        get {
            assertHasRole(Role.ADMIN)
            val incidents = incidentService.findAll()

            call.respond(incidents.map(Incident::toResponse))
        }
        get("/{id}") {
            val id: Long = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val foundIncident = incidentService.findById(id)
                ?: throw NotFoundException()

            val userId = call.userId()

            // a qualified official may get any Incident, a normal USER can only get own reported Incidents
            if (isQualifiedOfficial() || foundIncident.reportedBy == userId) {
                call.respond(foundIncident.toResponse())
            }

            return@get call.respond(HttpStatusCode.NotFound)
        }
        put("/{id}") {
            val incidentId: Long = call.parameters["id"]?.toLong()
                ?: throw NotFoundException()

            val incidentRequest = call.receive<IncidentRequest>()
            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()

            val canModify = isQualifiedOfficial() || foundIncident.reportedBy == userId
            if (!canModify)
                throw MissingRoleException()

            val changedIncident = incidentRequest.toModel(incidentId, userId)
            incidentService.save(changedIncident)

            call.respond(HttpStatusCode.OK)
        }
        patch("/{id}/{status}") {
            assertHasRole(Role.ADMIN)
            val incidentId: Long = call.parameters["id"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val status: Status = call.parameters["status"]
                ?.let(Status::valueOf)
                ?: throw BadRequestException("Invalid status")

            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            incidentService.changeStatus(foundIncident, status)

            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun IncidentRequest.toModel(incidentId: Long, userId: Long?): Incident =
    Incident(
        id = incidentId,
        reportedBy = userId,

        description = this.description,
        priority = this.priority,
        category = this.category,

        latitude = this.latitude,
        longitude = this.longitude
    )

fun Incident.toResponse(): IncidentResponse =
    IncidentResponse(
        id = this.id,
        reportedBy = this.reportedBy,

        description = this.description,
        priority = this.priority,
        category = this.category,
        latitude = this.latitude,
        longitude = this.longitude,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt
    )