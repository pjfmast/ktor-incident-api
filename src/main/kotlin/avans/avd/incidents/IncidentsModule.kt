package avans.avd.incidents

import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.incidentsModule() {
    val incidentService: IncidentService by inject()

    routing {
        route("/api/incident") {
            incidentRoute(incidentService)
        }
    }
}