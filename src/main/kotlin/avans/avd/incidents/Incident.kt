package avans.avd.incidents

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

enum class Priority {
    LOW, NORMAL, HIGH, CRITICAL
}

enum class Status { REPORTED, ASSIGNED, RESOLVED }

data class Incident(
    // the username of the user who reported this Incident
    val reportedBy: Long?,

    val category: String,
    val description: String,

    val latitude: Double,
    val longitude: Double,

    val priority: Priority = Priority.LOW,
    val status: Status = Status.REPORTED,
    val images: List<String> = emptyList(),

    // metadata about creating, updating and completing the Incident report
    val createdAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val updatedAt: LocalDateTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
    val completedAt: LocalDateTime? = null,

    val id: Long = NEW_INCIDENT_ID
) {
    val isAnonymous: Boolean
        get() = reportedBy == null

    val isResolved: Boolean
        get() = status == Status.RESOLVED

    val dueAt: LocalDateTime
        get() {
            // Convert LocalDateTime to Instant for duration calculations
            val systemTz = TimeZone.currentSystemDefault()
            val createdInstant = createdAt.toInstant(systemTz)

            // Apply the appropriate duration based on priority
            val dueInstant = when (priority) {
                Priority.LOW -> createdInstant.plus(42.days) // 6 weeks for low priority
                Priority.NORMAL -> createdInstant.plus(7.days) // one week for normal priority
                Priority.HIGH -> createdInstant.plus(3.days)
                Priority.CRITICAL -> createdInstant.plus(12.hours)
            }

            // Convert back to LocalDateTime
            return dueInstant.toLocalDateTime(systemTz)
        }

    fun isReportedByCurrentUser(userID: Long?): Boolean = userID != null && !isAnonymous && reportedBy == userID

    fun isCoordinateInArea(latMin: Double, latMax: Double, lngMin: Double, lngMax: Double): Boolean {
        return latitude in latMin..latMax && longitude in lngMin..lngMax
    }

    companion object {
        val NEW_INCIDENT_ID = 0L
    }
}