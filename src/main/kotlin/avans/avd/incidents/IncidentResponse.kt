package avans.avd.incidents

import avans.avd.incidents.Incident.Companion.NEW_INCIDENT_ID
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class IncidentResponse(
    val reportedBy: Long?, // user id of the user who reported this Incident

    val category: String,
    val description: String,

    val latitude: Double,
    val longitude: Double,

    val images: List<String>,

    val priority: Priority = Priority.Low,
    val status: Status = Status.OPEN,
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val updatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val completedAt: LocalDateTime? = null,

    val id: Long = NEW_INCIDENT_ID
)