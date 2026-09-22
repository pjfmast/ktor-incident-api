package avans.avd.incidents

import avans.avd.incidents.Incident.Companion.NEW_INCIDENT_ID
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class ReporterResponse(
    val id: Long,
    val username: String,
    val email: String,
    val avatar: String? = null
)

@Serializable
data class IncidentResponse(
    val reportedBy: Long?, // User ID of the user who reported this Incident
    val reporter: ReporterResponse? = null, // Nested reporter details for authorized officials/reporters

    val category: Category,
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
