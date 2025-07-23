package avans.avd.incidents

import avans.avd.auth.UserPrincipal
import avans.avd.core.PaginatedItemResponse
import avans.avd.exceptions.MissingRoleException
import avans.avd.users.Role
import avans.avd.utils.assertHasRole
import avans.avd.utils.assertIsQualified
import avans.avd.utils.isQualifiedOfficial
import avans.avd.utils.userId
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import kotlinx.datetime.toLocalDateTime
import java.io.File
import kotlin.time.Clock

fun Route.incidentRoutes(
    incidentService: IncidentService
) {
    // Anyone may create an Incident, if anonymous the issuer cannot edit the Incident later.
    // When an authenticated user creates an Incident, the userId identifies the user who created this Incident
    authenticate(optional = true) {
        post {
            val createIncidentRequest = call.receive<CreateIncidentRequest>()
            val userId = call.userId()

            val createdIncident = incidentService.save(
                createIncidentRequest.toModel(Incident.NEW_INCIDENT_ID, userId)
            )

            call.response.header(
                name = "id",
                value = createdIncident.id.toString()
            )
            call.respond(HttpStatusCode.Created, createdIncident.toResponse())
        }

        // any authenticated user can retrieve their own reported incidents
        get("/my-incidents") {
            // Get the current authenticated user from the principal
            val userPrincipal = call.principal<UserPrincipal>()

            userPrincipal?.let { user ->
                val userId = user.user.id
                val incidents = incidentService.findIncidentsReportedByUser(userId)

                call.respond(incidents.map(Incident::toResponse))
            } ?: call.respond(HttpStatusCode.Unauthorized, "Not authenticated")
        }
    }


    authenticate {
        post("/{incidentId}/images") {
            val incidentId: Long = call.parameters["incidentId"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()


            // a qualified official can document an incident with images, or any USER can document their own reported Incidents
            if (isQualifiedOfficial() || foundIncident.isReportedByCurrentUser(userId)) {
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

        // Only ADMIN or OFFICIAL may see all reported incidents
        get {
            assertIsQualified()
            val incidents = incidentService.findAll()

            call.respond(incidents.map(Incident::toResponse))
        }

        get("/paginated") {
            assertIsQualified()

            // Extract page and pageSize from query parameters with defaults
            val page = call.request.queryParameters["page"]?.toIntOrNull() ?: 1
            val pageSize = call.request.queryParameters["pageSize"]?.toIntOrNull() ?: 10

            // Validate parameters
            if (page <= 0 || pageSize <= 0) {
                call.respond(HttpStatusCode.BadRequest, "Page and pageSize must be positive")
                return@get
            }

            // Get paginated incidents
            val (incidents, totalCount) = incidentService.findAllPaginated(page, pageSize)

            // Map to response objects and wrap in PaginatedItemResponse
            val paginatedResponse = PaginatedItemResponse(
                data = incidents.map(Incident::toResponse),
                totalCount = totalCount.toInt()
            )

            call.respond(paginatedResponse)
        }


        get("/{incidentId}") {
            val incidentId: Long = call.parameters["incidentId"]?.toLongOrNull()
                ?: throw BadRequestException("Invalid ID")

            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()

            // a qualified official may get any Incident, a normal USER can only get own reported Incidents
            if (isQualifiedOfficial() || foundIncident.isReportedByCurrentUser(userId)) {
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

            // a qualified official may get any Incident, a normal USER can only get their own reported Incidents
            if (isQualifiedOfficial() || foundIncident.isReportedByCurrentUser(userId)) {
                incidentService.delete(incidentId)
                call.respond(HttpStatusCode.OK, "Incident with id: $incidentId is deleted.")
            }

            return@delete call.respond(HttpStatusCode.NotFound)
        }

        // an incident can be updated by an official or user who reported the incident,
        // but only if the incident is not resolved yet
        put("/{incidentId}") {
            val incidentId: Long = call.parameters["incidentId"]?.toLong()
                ?: throw NotFoundException()

            val updateRequest = call.receive<UpdateIncidentRequest>()
            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()

            val canModify = !foundIncident.isResolved
                    && (isQualifiedOfficial() || foundIncident.isReportedByCurrentUser(userId))
            if (!canModify)
                throw MissingRoleException(listOf(Role.OFFICIAL, Role.ADMIN))

            // Apply updates only to fields that are provided in the request
            val updatedIncident = foundIncident.copy(
                category = updateRequest.category ?: foundIncident.category,
                description = updateRequest.description ?: foundIncident.description,
                latitude = updateRequest.latitude ?: foundIncident.latitude,
                longitude = updateRequest.longitude ?: foundIncident.longitude,
                priority = updateRequest.priority ?: foundIncident.priority,
                updatedAt = Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            )

            val savedIncident = incidentService.save(updatedIncident)
            call.respond(HttpStatusCode.OK, savedIncident.toResponse())
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

private fun CreateIncidentRequest.toModel(incidentId: Long, userId: Long?): Incident =
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