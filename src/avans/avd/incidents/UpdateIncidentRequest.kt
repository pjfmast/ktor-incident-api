package avans.avd.incidents

import kotlinx.serialization.Serializable

@Serializable
data class UpdateIncidentRequest(
    val category: Category? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    // the property status and incident cannot be changed bij am update request
    // a user with a role = OFFICIAl can use ChangePriorityRequest and ChangeStatusRequest
)
