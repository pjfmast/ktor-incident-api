package avans.avd.users

import kotlinx.serialization.Serializable

@Serializable
data class CreateUserRequest(
    val username: String,
    val password: String,
    val email: String,
    val avatar: String? = null
)