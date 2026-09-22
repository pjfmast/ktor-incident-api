package avans.avd.incidents

import kotlinx.serialization.Serializable

@Serializable
data class ChangeStatusRequest(
    val status: Status
)