package avans.avd.users

import kotlinx.serialization.Serializable

@Serializable
data class RoleUpdateRequest(
    val role: Role
)