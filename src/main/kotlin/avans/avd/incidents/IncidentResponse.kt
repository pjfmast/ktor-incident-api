package avans.avd.incidents

import avans.avd.incidents.Incident.Companion.NEW_INCIDENT_ID
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
)
