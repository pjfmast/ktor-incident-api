package avans.avd.incidents

import kotlinx.serialization.Serializable

@Serializable
data class IncidentRequest (
    val category: String,
    val description: String,

    val latitude: Double,
    val longitude: Double,

    val priority: Priority = Priority.LOW
)
