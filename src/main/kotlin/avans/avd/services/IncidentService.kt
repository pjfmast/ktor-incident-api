package avans.avd.services

import avans.avd.models.Incident
import avans.avd.models.Status
import avans.avd.repositories.IncidentRepository

class IncidentService(
    private val incidentRepository: IncidentRepository<Long>,
) {
    suspend fun findAll(): List<Incident> =
        incidentRepository.findAll()

    suspend fun findById(id: Long): Incident? =
        incidentRepository.findById(id)

    suspend fun findIncidentsReportedByUser(userId: Long): List<Incident> =
        incidentRepository.findIncidentsForUser(userId)

    suspend fun save(incident: Incident): Incident =
        incidentRepository.save(incident)

    suspend fun changeStatus(incident: Incident, status: Status): Incident =
        incidentRepository.changeStatus(incident, status)

}