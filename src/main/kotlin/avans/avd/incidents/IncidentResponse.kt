package avans.avd.incidents

import avans.avd.incidents.Incident.Companion.NEW_INCIDENT_ID
import avans.avd.utils.toDefaultLocalDateTime
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class IncidentResponse(
    val reportedBy: Long?, // user id of the user who reported this Incident

    val category: String,
    val description: String,

    val latitude: Double,
    val longitude: Double,

    val images: List<String>,

    val priority: Priority,
    val status: Status,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val dueAt: LocalDateTime,
    val isAnonymous: Boolean,

    val id: Long = NEW_INCIDENT_ID
) {
    companion object {
        fun fromIncident(incident: Incident): IncidentResponse {
            return IncidentResponse(
                id = incident.id,
                reportedBy = incident.reportedBy,
                category = incident.category,
                description = incident.description,
                latitude = incident.latitude,
                longitude = incident.longitude,
                priority = incident.priority,
                status = incident.status,
                images = incident.images,
                createdAt = incident.createdAt.toDefaultLocalDateTime(),
                updatedAt = incident.updatedAt.toDefaultLocalDateTime(),
                completedAt = incident.completedAt?.toDefaultLocalDateTime(),
                dueAt = incident.dueAt.toDefaultLocalDateTime(),
                isAnonymous = incident.isAnonymous
            )
        }
    }
}
