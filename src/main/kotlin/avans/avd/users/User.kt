package avans.avd.users


data class User(
    val username: String,
    val password: String,
    val email: String,
    val role: Role = Role.USER,
    val avatar: String? = null,
    val id: Long = NEW_USER_ID
) {
    companion object {
        const val NEW_USER_ID = 0L
    }
}

enum class Role {
    USER,
    OFFICIAL,
    ADMIN
}