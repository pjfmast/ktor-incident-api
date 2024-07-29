package avans.avd.models

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

enum class Priority {
    Low, Medium, High, Vital
}

enum class Status { OPEN, ONGOING, DONE }

data class Incident(
    val id: Long = NEW_INCIDENT_ID,

    // the username of the user who reported this Incident
    val reportedBy: Long?,

    val category: String,
    val description: String,

    val latitude: Double,
    val longitude: Double,

    val priority: Priority = Priority.Low,
    val status: Status = Status.OPEN,
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val updatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val completedAt: LocalDateTime? = null,
) {
    companion object {
        val NEW_INCIDENT_ID = 0L
    }
}