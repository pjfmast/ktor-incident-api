package avans.avd.incidents

import kotlinx.serialization.Serializable

@Serializable
data class CreateIncidentRequest (
    val category: Category,
    val description: String,

    val latitude: Double,
    val longitude: Double,

    val priority: Priority = Priority.LOW
)
