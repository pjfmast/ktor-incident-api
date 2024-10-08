package avans.avd.incidents

import avans.avd.exceptions.MissingRoleException
import avans.avd.users.Role
import avans.avd.utils.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import java.io.File

fun Route.incidentRoutes(
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
        post("/{incidentId}/images") {
            val incidentId: Long = call.parameters["incidentId"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()


            // a qualified official can document an incident with pictures or any USER can document own reported Incidents
            if (isQualifiedOfficial() || foundIncident.reportedBy == userId) {
                var imageFileDescription = ""
                var imageFileName = ""

                val multipartData = call.receiveMultipart()

                multipartData.forEachPart { part ->
                    when (part) {
                        is PartData.FormItem -> {
                            imageFileDescription = part.value
                        }

                        is PartData.FileItem -> {
                            val nextIncidentImageNr =
                                foundIncident.images.size + 1 // images are not deleted (yet), but safer to increment the max imageNr?

                            val extension: String = part.originalFileName?.split(".")?.last() ?: "png"
                            imageFileName = "incident${incidentId}-image$nextIncidentImageNr.$extension"
                            val fileBytes = part.provider().toByteArray()
                            File(getImageUploadPath(imageFileName)).writeBytes(fileBytes)
                            incidentService.addImage(incidentId, imageFileName)
                        }

                        else                 -> {}
                    }
                    part.dispose()
                }
                call.respond(
                    HttpStatusCode.OK,
                    "$imageFileDescription is uploaded for incident with id: ${incidentId} to ${
                        getImageUploadPath(
                            imageFileName
                        )
                    }"
                )
            }
        }

        get {
            assertHasRole(Role.ADMIN)
            val incidents = incidentService.findAll()

            call.respond(incidents.map(Incident::toResponse))
        }

        get("/{incidentId}") {
            val incidentId: Long = call.parameters["incidentId"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()

            // a qualified official may get any Incident, a normal USER can only get own reported Incidents
            if (isQualifiedOfficial() || foundIncident.reportedBy == userId) {
                call.respond(foundIncident.toResponse())
            }

            return@get call.respond(HttpStatusCode.NotFound)
        }

        delete("/{incidentId}") {
            val incidentId: Long = call.parameters["incidentId"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()

            // a qualified official may get any Incident, a normal USER can only get own reported Incidents
            if (isQualifiedOfficial() || foundIncident.reportedBy == userId) {
                incidentService.delete(incidentId)
                call.respond(HttpStatusCode.OK, "Incident with id: $incidentId is deleted.")
            }

            return@delete call.respond(HttpStatusCode.NotFound)
        }

        put("/{incidentId}") {
            val incidentId: Long = call.parameters["incidentId"]?.toLong()
                ?: throw NotFoundException()

            val incidentRequest = call.receive<IncidentRequest>()
            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()

            val canModify = isQualifiedOfficial() || foundIncident.reportedBy == userId
            if (!canModify)
                throw MissingRoleException(listOf(Role.OFFICIAL, Role.ADMIN))

            val changedIncident = incidentRequest.toModel(incidentId, userId)
            incidentService.save(changedIncident)

            call.respond(HttpStatusCode.OK)
        }

        patch("/{incidentId}/{status}") {
            assertHasRole(Role.ADMIN, Role.OFFICIAL)
            val incidentId: Long = call.parameters["incidentId"]?.toLongOrNull()
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
        reportedBy = this.reportedBy,
        category = this.category,

        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        images = this.images,
        priority = this.priority,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt,
        completedAt = this.completedAt,
        id = this.id
    )