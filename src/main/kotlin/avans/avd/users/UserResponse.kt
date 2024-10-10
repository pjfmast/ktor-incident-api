package avans.avd.users

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val username: String,
    val email: String,
    val role: Role,
    val avatar: String,
    val id: String,
)