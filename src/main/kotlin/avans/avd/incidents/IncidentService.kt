package avans.avd.incidents

import java.nio.file.Files.deleteIfExists
import kotlin.io.path.Path

class IncidentService(
    private val incidentRepository: IncidentRepository<Long>,
) {
    suspend fun findAll(): List<Incident> =
        incidentRepository.findAll()

    suspend fun findAllPaginated(page: Int, pageSize: Int): Pair<List<Incident>, Long> {
        return incidentRepository.findAllPaginated(page, pageSize)
    }

    suspend fun findById(id: Long): Incident? =
        incidentRepository.findById(id)

    suspend fun findIncidentsReportedByUser(userId: Long): List<Incident> =
        incidentRepository.findIncidentsForUser(userId)

    suspend fun save(incident: Incident): Incident =
        incidentRepository.save(incident)

    suspend fun delete(incidentId: Long): Boolean {
        val foundIncident = incidentRepository.findById(incidentId)
        return if (foundIncident != null) {
            incidentRepository.delete(incidentId)
            // also remove all images for this incident
            foundIncident.images.forEach { imagefile ->
                val imageToDelete = Path(getImageUploadPath(imagefile))
                deleteIfExists(imageToDelete)
            }
            true
        } else false
    }

    suspend fun changeStatus(incident: Incident, status: Status): Incident =
        incidentRepository.changeStatus(incident, status)

    suspend fun addImage(incidentId: Long, imageFileName: String) =
        incidentRepository.addImage(incidentId, imageFileName)

}