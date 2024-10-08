package avans.avd.incidents

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class Priority {
    Low, Medium, High, Vital
}

enum class Status { OPEN, ASSIGNED, DONE }

data class Incident(
    // the username of the user who reported this Incident
    val reportedBy: Long?,

    val category: String,
    val description: String,

    val latitude: Double,
    val longitude: Double,

    val priority: Priority = Priority.Low,
    val status: Status = Status.OPEN,
    val images: MutableList<String> = mutableListOf(),

    // metadata about creating, updating and completing the Incident report
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val updatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val completedAt: LocalDateTime? = null,

    val id: Long = NEW_INCIDENT_ID
) {
    companion object {
        val NEW_INCIDENT_ID = 0L
    }
}