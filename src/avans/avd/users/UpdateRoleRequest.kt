package avans.avd.users

import kotlinx.serialization.Serializable

@Serializable
data class UpdateRoleRequest(
    val role: Role
)
