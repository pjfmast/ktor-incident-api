package avans.avd.users


data class User(
    val id: Long = NEW_USER_ID,
    val username: String,
    val password: String,
    val email: String,
    val role: Role = Role.USER,
    val avatar: String? = null
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