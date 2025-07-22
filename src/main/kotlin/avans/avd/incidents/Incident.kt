package avans.avd.incidents

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

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

    fun isReportedByCurrentUser(userID: Long?): Boolean = userID != null && !isAnonymous && reportedBy == userID

    fun isCoordinateInArea(latMin: Double, latMax: Double, lngMin: Double, lngMax: Double): Boolean {
        return latitude in latMin..latMax && longitude in lngMin..lngMax
    }

    companion object {
        val NEW_INCIDENT_ID = 0L
    }
}