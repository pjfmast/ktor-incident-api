package avans.avd.incidents

import avans.avd.utils.currentInstant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

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
    val createdAt: Instant = currentInstant(),
    val updatedAt: Instant = currentInstant(),
    val completedAt: Instant? = null,

    val id: Long = NEW_INCIDENT_ID
) {
    val isAnonymous: Boolean
        get() = reportedBy == null

    val isResolved: Boolean
        get() = status == Status.RESOLVED

    val dueAt: Instant
        get() {
            // Apply the appropriate duration based on priority
            return when (priority) {
                Priority.LOW -> createdAt.plus(42.days)  // 6 weeks = 42 days
                Priority.NORMAL -> createdAt.plus(7.days)  // 1 week = 7 days
                Priority.HIGH -> createdAt.plus(3.days)
                Priority.CRITICAL -> createdAt.plus(12.hours)
            }
        }


    fun isReportedByCurrentUser(userID: Long?): Boolean = userID != null && !isAnonymous && reportedBy == userID

    fun isCoordinateInArea(latMin: Double, latMax: Double, lngMin: Double, lngMax: Double): Boolean {
        return latitude in latMin..latMax && longitude in lngMin..lngMax
    }

    fun isDueOrOverdue(): Boolean {
        val now = currentInstant()
        return !isResolved && now >= dueAt
    }


    companion object {
        val NEW_INCIDENT_ID = 0L
    }
}