package avans.avd.users

import io.ktor.server.auth.*

data class User(
    val username: String,
    val password: String,
    val email: String,
    val role: Role = Role.USER,
    val avatar: String? = null,
    // Deliberately a primitive Long and not a value class UserId: ids cross the JSON, JWT-claim and
    // route-parameter boundaries, each of which would need its own serializer and conversion.
    val id: Long = NEW_USER_ID
) {
    companion object {
        const val NEW_USER_ID = 0L
    }
}

// Roles are hierarchical: ADMIN implies OFFICIAL, OFFICIAL implies USER (see JwtService.roleAuth)
enum class Role : AuthenticationRole {
    USER,
    OFFICIAL,
    ADMIN;

    /** All roles this role implies, including itself. */
    val implied: Set<Role>
        get() = entries.filter { it.ordinal <= ordinal }.toSet()
}
