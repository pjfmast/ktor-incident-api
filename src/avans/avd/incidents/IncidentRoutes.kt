package avans.avd.incidents

import avans.avd.auth.RoleAuthScheme
import avans.avd.core.PaginatedItemResponse
import avans.avd.users.Role
import avans.avd.users.User
import avans.avd.users.UserService
import avans.avd.utils.currentInstant
import avans.avd.utils.isQualifiedOfficial
import avans.avd.utils.toDefaultLocalDateTime
import avans.avd.utils.userId
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import java.io.File

// For now, only light validation that these files are images.
// In real production, be sure to validate the file contents later with Magic Bytes validation and decompression.
private val ALLOWED_IMAGE_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp")
private val IMAGE_INDEX_REGEX = Regex("""-image(\d+)\.""")

private fun getNextImageNumber(incidentId: Long, currentImages: List<String>, startFrom: Int = 1): Int {
    val maxDbIndex = currentImages.mapNotNull { name ->
        IMAGE_INDEX_REGEX.find(name)?.groupValues?.get(1)?.toIntOrNull()
    }.maxOrNull() ?: 0

    var nextNr = maxOf(startFrom, maxDbIndex + 1)

    while (ALLOWED_IMAGE_EXTENSIONS.any { ext ->
        File(getImageUploadPath("incident$incidentId-image$nextNr.$ext")).exists()
    }) {
        nextNr++
    }

    return nextNr
}

fun Route.incidentRoutes(
    incidentService: IncidentService,
    userService: UserService,
    roleAuth: RoleAuthScheme
) {
    // Anyone may create an Incident, if anonymous the issuer cannot edit the Incident later.
    // When an authenticated user creates an Incident, the userId identifies the user who created this Incident
    authenticateWithOptional(roleAuth) {
        post {
            val createIncidentRequest = call.receive<CreateIncidentRequest>()
            val userId: Long? = call.userId()

            val createdIncident = incidentService.save(
                createIncidentRequest.toModel(Incident.NEW_INCIDENT_ID, userId)
            )

            call.response.header(
                name = "id",
                value = createdIncident.id.toString()
            )
            call.respond(HttpStatusCode.Created, createdIncident.toResponse())
        }

        post("/{incidentId}/images") {
            val incidentId: Long by call.pathParameters

            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException("Incident with id $incidentId not found")

            var fileDescription = ""
            val uploadedFileNames = mutableListOf<String>()
            var nextIncidentImageNr = getNextImageNumber(incidentId, foundIncident.images)
            var invalidExtension: String? = null

            val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)

            multipartData.forEachPart { part ->
                if (invalidExtension != null) {
                    part.release()
                    return@forEachPart
                }

                when (part) {
                    is PartData.FormItem -> {
                        fileDescription = part.value
                    }

                    is PartData.FileItem -> {
                        val rawFileName = part.originalFileName ?: ""
                        val rawExtension = File(rawFileName).extension.lowercase()

                        if (rawExtension !in ALLOWED_IMAGE_EXTENSIONS) {
                            call.application.environment.log.warn(
                                "Rejected upload for incident $incidentId: unsupported extension '$rawExtension' from '$rawFileName'"
                            )
                            invalidExtension = rawExtension
                            part.release()
                            return@forEachPart
                        }

                        // Ensure destination directory exists
                        val uploadDir = File(INCIDENT_IMAGES_DIR)
                        if (!uploadDir.exists()) {
                            uploadDir.mkdirs()
                        }

                        nextIncidentImageNr = getNextImageNumber(incidentId, foundIncident.images, nextIncidentImageNr)
                        val fileName = "incident${incidentId}-image$nextIncidentImageNr.$rawExtension"
                        val destinationFile = File(getImageUploadPath(fileName))

                        call.application.environment.log.info(
                            "Uploading image for incident $incidentId: $fileName (original: $rawFileName)"
                        )

                        part.provider().copyAndClose(destinationFile.writeChannel())

                        incidentService.addImage(incidentId, fileName)
                        uploadedFileNames.add(fileName)

                        nextIncidentImageNr++
                    }

                    else -> {}
                }
                part.release()
            }

            if (invalidExtension != null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Invalid file type '$invalidExtension'. Allowed types: ${ALLOWED_IMAGE_EXTENSIONS.joinToString(", ")}"
                )
                return@post
            }
            if (uploadedFileNames.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "No files were uploaded")
            } else if (uploadedFileNames.size == 1) {
                call.respond(
                    HttpStatusCode.OK,
                    "${fileDescription.ifBlank { "Image" }} is uploaded for incident with id: $incidentId to ${
                        getImageUploadPath(uploadedFileNames[0])
                    }"
                )
            } else {
                call.respond(
                    HttpStatusCode.OK,
                    "${uploadedFileNames.size} images uploaded for incident with id: $incidentId"
                )
            }
        }
    }

    // Only ADMIN or OFFICIAL (a "qualified official") may access these routes;
    // Ktor responds with 403 Forbidden for any other authenticated user.
    authenticateWith(roleAuth, roles = setOf(Role.OFFICIAL)) {
        // see all reported incidents
        get {
            val incidents = incidentService.findAll()

            call.respond(incidents.map { it.toResponse() })
        }

        get("/paginated") {
            val page: Int? by call.queryParameters
            val pageSize: Int? by call.queryParameters

            val currentPage = page ?: 1
            val currentPageSize = pageSize ?: 10

            // Validate parameters
            if (currentPage <= 0 || currentPageSize <= 0) {
                call.respond(HttpStatusCode.BadRequest, "Page and pageSize must be positive")
                return@get
            }

            // Get paginated incidents
            val (incidents, totalCount) = incidentService.findAllPaginated(currentPage, currentPageSize)

            // Map to response objects and wrap in PaginatedItemResponse
            val paginatedResponse = PaginatedItemResponse(
                data = incidents.map { it.toResponse() },
                totalCount = totalCount.toInt()
            )

            call.respond(paginatedResponse)
        }

        // Endpoint to change incident priority
        patch("/{incidentId}/priority") {
            val incidentId: Long by call.pathParameters

            val changePriorityRequest = call.receive<ChangePriorityRequest>()
            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val updatedIncident = foundIncident.copy(
                priority = changePriorityRequest.priority,
                updatedAt = currentInstant()
            )

            val savedIncident = incidentService.save(updatedIncident)
            call.respond(HttpStatusCode.OK, savedIncident.toResponse())
        }

        // Endpoint to change incident status
        patch("/{incidentId}/status") {
            val incidentId: Long by call.pathParameters

            val changeStatusRequest = call.receive<ChangeStatusRequest>()
            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val updatedIncident = incidentService.changeStatus(foundIncident, changeStatusRequest.status)
            call.respond(HttpStatusCode.OK, updatedIncident.toResponse())
        }
    }

    // Routes for any authenticated user; ownership is checked per incident
    authenticateWith(roleAuth) {

        // any authenticated user can retrieve their own reported incidents
        get("/my-incidents") {
            // Get the current authenticated user from the principal
            val userPrincipal = call.principal
            val userId = userPrincipal.user.id
            val incidents = incidentService.findIncidentsReportedByUser(userId)

            call.respond(incidents.map { it.toResponse() })
        }

        get("/{incidentId}") {
            val incidentId: Long by call.pathParameters

            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()

            // A qualified official may get any Incident, a normal USER can only get their own reported Incidents
            if (isQualifiedOfficial() || foundIncident.isReportedByCurrentUser(userId)) {
                // Fetch reporter details if the incident was reported by an authenticated user
                val reporter = foundIncident.reportedBy?.let { reporterId ->
                    userService.findById(reporterId)
                }
                call.respond(foundIncident.toResponse(reporter))
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete("/{incidentId}") {
            val incidentId: Long by call.pathParameters
            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()
            val hasPermission = isQualifiedOfficial() || foundIncident.isReportedByCurrentUser(userId)

            // 1. Guard clause: exit early if the user is not authorized to delete this incident
            if (!hasPermission) {
                throw NotFoundException("Incident with id $incidentId not found")
            }

            // 2. Execute the delete operation and handle the result explicitly
            val deleteIsSuccess = incidentService.delete(incidentId)
            if (!deleteIsSuccess) {
                throw IllegalStateException("Failed to delete incident with id $incidentId")
            }

            call.respond(HttpStatusCode.NoContent)
        }

        // an incident can be updated by an official or user who reported the incident,
        // but only if the incident is not resolved yet
        put("/{incidentId}") {
            val incidentId: Long by call.pathParameters

            val updateRequest = call.receive<UpdateIncidentRequest>()
            val foundIncident = incidentService.findById(incidentId)
                ?: throw NotFoundException()

            val userId = call.userId()

            val canModify = !foundIncident.isResolved
                    && (isQualifiedOfficial() || foundIncident.isReportedByCurrentUser(userId))
            if (!canModify) {
                return@put call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Only an official or the user who reported this incident may modify it, and only while it is unresolved.")
                )
            }

            // Apply updates only to fields that are provided in the request
            val updatedIncident = foundIncident.copy(
                category = updateRequest.category ?: foundIncident.category,
                description = updateRequest.description ?: foundIncident.description,
                latitude = updateRequest.latitude ?: foundIncident.latitude,
                longitude = updateRequest.longitude ?: foundIncident.longitude,
                updatedAt = currentInstant()
            )

            val savedIncident = incidentService.save(updatedIncident)
            call.respond(HttpStatusCode.OK, savedIncident.toResponse())
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

fun Incident.toResponse(user: User? = null): IncidentResponse =
    IncidentResponse(
        reportedBy = this.reportedBy,
        reporter = user?.let {
            ReporterResponse(
                id = it.id,
                username = it.username,
                email = it.email,
                avatar = it.avatar
            )
        },
        category = this.category,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        images = this.images,
        priority = this.priority,
        status = this.status,
        createdAt = this.createdAt.toDefaultLocalDateTime(),
        updatedAt = this.updatedAt.toDefaultLocalDateTime(),
        completedAt = this.completedAt?.toDefaultLocalDateTime(),
        dueAt = this.dueAt.toDefaultLocalDateTime(),
        isAnonymous = this.isAnonymous,
        id = this.id
    )
