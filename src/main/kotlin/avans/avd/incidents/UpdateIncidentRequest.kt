package avans.avd.incidents

import kotlinx.serialization.Serializable

@Serializable
data class UpdateIncidentRequest(
    val category: String? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    // the property status and incident cannot be changed bij am update request
    // a user with role=OFFICIAl can use ChangePriorityRequest and ChangeStatusRequest
)
