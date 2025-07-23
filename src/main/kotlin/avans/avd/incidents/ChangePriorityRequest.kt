package avans.avd.incidents

import kotlinx.serialization.Serializable

@Serializable
data class ChangePriorityRequest (
    val priority: Priority
)