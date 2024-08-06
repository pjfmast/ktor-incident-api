package avans.avd.incidents

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import java.io.File

fun Application.incidentsModule() {
    val incidentService: IncidentService by inject()

    routing {
        staticFiles("/images", File("uploads"), "incident.png") {
        }
        route("/api/incidents") {
            incidentRoutes(incidentService)
        }
    }
}