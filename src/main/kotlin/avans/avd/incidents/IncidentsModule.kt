package avans.avd.incidents

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import java.io.File

@Suppress("unused")
fun Application.incidentsModule(incidentService: IncidentService) {

    // Ensure the upload directory exists at application startup
    val uploadsDir = File("uploads/incidentsImages")
    if (!uploadsDir.exists()) {
        uploadsDir.mkdirs()
    }

    routing {
        staticFiles(
            remotePath = "api/incidents/images",
            dir = File("uploads/incidentsImages"),
            index = "incident.png"
        ) {
            default("incident.png")
        }
        route("/api/incidents") {
            incidentRoutes(incidentService)
        }
    }
}

fun getImageUploadPath(imageFile: String) = "uploads/incidentsImages/$imageFile"
