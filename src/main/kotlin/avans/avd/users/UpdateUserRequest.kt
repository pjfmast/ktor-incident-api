package avans.avd.users

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val username: String? = null,
    val password: String? = null,
    val email: String? = null,
    val avatar: String? = null
)
