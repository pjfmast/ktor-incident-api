package avans.avd.incidents

import kotlinx.serialization.Serializable

@Serializable
data class UpdateIncidentRequest(
    val category: String? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val priority: Priority? = null
)
