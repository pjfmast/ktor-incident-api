package avans.avd.incidents

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime


object FakeIncidentRepository : IncidentRepository<Long> {
    private var incidentId: Long = 0L
    private var imageId: Long = 0L
    private val incidents = mutableListOf<Incident>()

    // Seed the fake repository with some dummy data
    init {
        create(
            Incident(
                Incident.NEW_INCIDENT_ID,
                1 /*admin*/,
                "Traffic",
                "Sink hole here. Dangerous situation! Quick fix needed.",
                51.58677130730741,
                4.808487370673,
                Priority.High,
                Status.ASSIGNED,
            )
        )
        create(
            Incident(
                Incident.NEW_INCIDENT_ID,
                2 /*Henk*/,
                "Street lights",
                "At this corner 2 lights are broken",
                51.59051650746655,
                4.812002566502519,
                Priority.Medium
            )
        )
        create(
            Incident(
                Incident.NEW_INCIDENT_ID,
                2 /*Henk*/,
                "neighbourhood",
                "noise disturbance from illegal party",
                51.58218477578439,
                4.835727885428926,
                Priority.Medium
            )
        )
        create(
            Incident(
                Incident.NEW_INCIDENT_ID,
                3 /*Sophie*/,
                "Garbage dump",
                "Some xtc lab dumped chemicals. ",
                51.58907773104348,
                4.80552621192238,
                Priority.High
            )
        )
    }

    override suspend fun findIncidentsForUser(userID: Long): List<Incident> =
        incidents.filter { it.reportedBy == userID }


    override suspend fun findAll(): List<Incident> = incidents.toList()

    override suspend fun findById(id: Long): Incident? = incidents.find { it.id == id }

    override suspend fun changeStatus(incident: Incident, status: Status): Incident {
        val changedIncident = if (status == Status.DONE) {
            incident.copy(
                status = status,
                completedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            )
        } else incident.copy(
            status = status,
        )

        return update(changedIncident)
    }

    override suspend fun addImage(id: Long, imageFileName: String) {
        val incident = findById(id)
        if (incident != null) {
            imageId++

            incident.images.add(imageFileName)
        }
    }

    override suspend fun save(entity: Incident): Incident {
        return if (incidents.any { it.id == entity.id }) {
            update(entity)
        } else {
            create(entity)
        }
    }

    override suspend fun saveAll(entities: Iterable<Incident>): List<Incident> = entities.map { save(it) }

    private fun create(incident: Incident): Incident {
        incidentId++
        val newIncident = incident.copy(id = incidentId)
        incidents.add(newIncident)
        return newIncident
    }

    override suspend fun delete(entity: Incident): Boolean = incidents.removeIf { it.id == entity.id }

    private fun update(incident: Incident): Incident {
        require(incidents.any { it.id == incident.id }) { "not an update: ${incident.id} does not exist" }

        incidents.removeIf { it.id == incident.id }
        val changedIncident =
            incident.copy(updatedAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
        incidents.add(changedIncident)
        return changedIncident
    }
}