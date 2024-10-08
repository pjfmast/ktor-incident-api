package avans.avd.incidents

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File

@Suppress("unused")
fun Application.incidentsModule() {
    val incidentService: IncidentService by inject()

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
