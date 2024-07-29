package avans.avd.dto

import avans.avd.models.Role
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val role: Role
)