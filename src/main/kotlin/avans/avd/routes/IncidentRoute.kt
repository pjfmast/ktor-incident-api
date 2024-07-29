package avans.avd.routes

import avans.avd.dto.IncidentRequest
import avans.avd.dto.IncidentResponse
import avans.avd.models.Incident
import avans.avd.models.Role
import avans.avd.models.Status
import avans.avd.models.isQualifiedOfficial
import avans.avd.plugins.extractRoleFromToken
import avans.avd.plugins.extractUserIdFromToken
import avans.avd.services.IncidentService
import avans.avd.util.authorized
import avans.avd.util.extractPrincipalId
import io.ktor.http.*
import io.ktor.server.auth.*
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
            val userId = extractPrincipalId(call)

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
        authorized(Role.ADMIN) {
            get {
                val incidents = incidentService.findAll()

                call.respond(incidents.map(Incident::toResponse))
            }
        }
    }

    authenticate {
        get("/{id}") {
            val id: Long = call.parameters["id"]?.toLong()
                ?: return@get call.respond(HttpStatusCode.BadRequest)

            val foundIncident = incidentService.findById(id)
                ?: return@get call.respond(HttpStatusCode.NotFound)


            val role = extractRoleFromToken(call)
            val userId = extractUserIdFromToken(call)

            // a qualified official may get any Incident, a normal USER can only get own reported Incidents
            if (role.isQualifiedOfficial() || foundIncident.reportedBy == userId) {
                call.respond(foundIncident.toResponse())
            }

            return@get call.respond(HttpStatusCode.NotFound)
        }
    }

    authenticate {
        put("/{id}") {
            val incidentId: Long = call.parameters["id"]?.toLong()
                ?: return@put call.respond(HttpStatusCode.BadRequest)

            val incidentRequest = call.receive<IncidentRequest>()
            val foundIncident = incidentService.findById(incidentId)
                ?: return@put call.respond(HttpStatusCode.NotFound)

            val role = extractRoleFromToken(call)
            val userId = extractUserIdFromToken(call)

            if (role.isQualifiedOfficial() || foundIncident.reportedBy == userId) {
                val changedIncident = incidentRequest.toModel(incidentId, userId)
                incidentService.save(changedIncident)

                call.respond(HttpStatusCode.OK)
            }

            return@put call.respond(HttpStatusCode.NotFound)
        }
    }

    authenticate {
        authorized(Role.ADMIN, Role.OFFICIAL) {
            patch("/{id}/{status}") {
                val incidentId: Long = call.parameters["id"]?.toLong()
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)

                val newStatus: String = call.parameters["status"]
                    ?: return@patch call.respond(HttpStatusCode.BadRequest)

                val status = try {
                    Status.valueOf(newStatus)
                } catch (e: IllegalArgumentException) {
                    return@patch call.respond(HttpStatusCode.BadRequest)
                }

                val foundIncident = incidentService.findById(incidentId)
                    ?: return@patch call.respond(HttpStatusCode.NotFound)

                foundIncident.let {
                    incidentService.changeStatus(foundIncident, status)

                    call.respond(HttpStatusCode.OK)
                }

                return@patch call.respond(HttpStatusCode.NotFound)
            }
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